package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.InviteRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class InviteService extends BaseService<Invite, InviteRepository> {
    @Autowired
    RoleService roleService;

    @Autowired
    UserService userService;

    public List<Invite> searchCreated(String userId, Specification<Invite> specification) {
        return super.search(InviteSpecs.isOwnedBy(userId).and(specification));
    }

    public List<Invite> searchInvited(String userId, Specification<Invite> specification) {
        User user = userService.get(userId);
        String email = user.getEmail();
        return super.search(InviteSpecs.isInvited(email).and(specification));
    }

    public List<Invite> searchByEmail(String email) {
        return super.search(InviteSpecs.isInvited(email));
    }

    @Transactional
    public Invite createInvite(String email, Collection<UserType> userTypes, String ownerId) {
        User owner = userService.get(ownerId);
        checkIfCanInviteUserTypes(owner.getRoles(), userTypes);
        checkIfExistsOpenInvite(email, ownerId);
        checkInviteeDoesNotHaveRoles(email, userTypes);
        Invite invite = new Invite(email);
        for (UserType type : userTypes) {
            Role role = roleService.get(type);
            invite.addRole(role);
        }
        owner.addInvite(invite);
        invite.setOwner(owner);
        return invite;
    }

    private void checkInviteeDoesNotHaveRoles(String email, Collection<UserType> invitationUserTypes) {
        User invitee = userService.getByEmail(email);
        if(invitee != null) {
            Boolean containsAll = true;
            for(UserType ut: invitationUserTypes) {
                Boolean hasUt = invitee.isUserType(ut);
                if(!hasUt) {
                    containsAll = false;
                    break;
                }
            }
            if(containsAll)
                throw new ServiceException("User already exists with this role");
        }
    }

    private void checkIfExistsOpenInvite(String email, String ownerId) {
        Specification<Invite> spec = InviteSpecs.isInvited(email)
                .and(InviteSpecs.isOwnedBy(ownerId))
                .and(InviteSpecs.isPending())
                .and(InviteSpecs.notForCourseYear());
        List<Invite> invites = repo.findAll(spec);
        if(invites.size() > 0) {
            throw new ServiceException("Invitation for this email already exists");
        }
    }

    private void checkIfCanInviteUserTypes(Set<Role> ownerRoles, Collection<UserType> invitationUserTypes) {
        boolean canInvite = false;
        for (Role ownerRole : ownerRoles) {
            canInvite = invitationUserTypes.stream()
                    .allMatch(iUT -> canInviteRole(ownerRole.getUserType(), iUT));
            if(canInvite) {
                break;
            }
        }
        if(!canInvite) {
            throw new ServiceException("User is not allowed to create an invitation for this role");
        }
    }

    private boolean canInviteRole(UserType ownerType, UserType invitationRole) {
        List<UserType> allowedRoles = new ArrayList<UserType>();
        switch (ownerType) {
            case ADMIN:
                allowedRoles.add(UserType.ADMIN);
                allowedRoles.add(UserType.PROFESSOR);
                break;
            case PROFESSOR:
                allowedRoles.add(UserType.PROFESSOR);
                allowedRoles.add(UserType.STUDENT);
                break;
            case STUDENT:
                break;
        }
        boolean canInvite = allowedRoles.stream().anyMatch(s -> s == invitationRole);
        return canInvite;
    }

    @Transactional
    public void deleteInvite(Long inviteId, String userId) {
        Invite invite = super.get(inviteId);
        if(!invite.getOwnerId().equals(userId)) {
            throw new ServiceException("User cannot manage invite");
        }
        if(invite.getState() != InviteState.PENDING) {
            throw new ServiceException("Only pending invites can be deleted");
        }
        repo.delete(invite);
    }

    @Transactional
    public void acceptInvite(Long inviteId, String userId) {
        Invite invite = get(inviteId);
        User user = userService.get(userId);
        useInvite(invite, user);
    }

    @Transactional
    public void useInvite(Invite invite, User user) {
        if(!user.getEmail().equals(invite.getEmail())) {
            throw new ServiceException("User cannot accept an invite that is not for them");
        }
        if(invite.getState() != InviteState.PENDING) {
            throw new ServiceException("Invite cannot be used");
        }
        for(Role inviteRole : invite.getRoles()) {
            user.addRole(inviteRole);
        }
        if(invite.getCourseYear() != null) {
            user.addToCourse(invite.getCourseYear());
        }
        invite.use();
    }
}
