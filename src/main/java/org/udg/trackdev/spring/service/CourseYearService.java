package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.BaseRepositoryLong;
import org.udg.trackdev.spring.repository.CourseYearRepository;
import org.udg.trackdev.spring.repository.InviteRepository;

import java.util.List;

@Service
public class CourseYearService extends BaseService<CourseYear, CourseYearRepository> {

    @Autowired
    CourseService courseService;

    @Autowired
    UserService userService;

    @Autowired
    InviteService inviteService;

    @Transactional
    public CourseYear createCourseYear(Long courseId, Integer startYear, String loggedInUserId) {
        Course course = courseService.getCourse(courseId);
        if(!courseService.canManageCourse(course, loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
        CourseYear courseYear = new CourseYear(startYear);
        courseYear.setCourse(course);
        course.addCourseYear(courseYear);
        return courseYear;
    }

    public void deleteCourseYear(Long yearId, String loggedInUserId) {
        CourseYear courseYear = get(yearId);
        if(!courseService.canManageCourse(courseYear.getCourse(), loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
        repo.delete(courseYear);
    }

    @Transactional
    public Invite createInvite(String email, Long yearId, String ownerId) {
        CourseYear courseYear = get(yearId);
        if(!courseService.canManageCourse(courseYear.getCourse(), ownerId)) {
            throw new ServiceException("User cannot manage this course");
        }
        alreadyInCoureYear(email, yearId);
        checkIfExistsOpenInvite(email, ownerId, yearId);

        User owner = userService.get(ownerId);
        Invite invite = new Invite(email);
        invite.setCourseYear(courseYear);
        owner.addInvite(invite);
        invite.setOwner(owner);

        return invite;
    }

    private void alreadyInCoureYear(String email, Long yearId) {
        User invitee = userService.getByEmail(email);
        if(invitee != null) {
            for(CourseYear cy: invitee.getCourseYears()) {
                if(cy.getId().equals(yearId)) {
                    throw new ServiceException("User is already enrolled in course");
                }
            }
        }
    }

    private void checkIfExistsOpenInvite(String email, String ownerId, Long yearId) {
        Specification<Invite> spec = InviteSpecs.isInvited(email)
                .and(InviteSpecs.isPending())
                .and(InviteSpecs.forCourseYear(yearId));

        List<Invite> invites = inviteService.searchCreated(ownerId, spec);
        if(invites.size() > 0) {
            throw new ServiceException("Invitation for this email already exists");
        }
    }
}
