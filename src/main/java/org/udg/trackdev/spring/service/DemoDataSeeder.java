package org.udg.trackdev.spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class DemoDataSeeder {

    private final Logger logger = LoggerFactory.getLogger(Global.class);

    @Autowired
    Global global;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseYearService courseYearService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private IterationService iterationService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private BacklogService backlogService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private TaskService taskService;

    public void seedDemoData() {
        logger.info("Starting populating database ...");
        // users
        User admin = userService.addUserInternal("neich", "ignacio.martin@udg.edu", global.getPasswordEncoder().encode("123456"), List.of(UserType.ADMIN, UserType.PROFESSOR));
        User student1 = userService.addUserInternal("student1", "student1@trackdev.com", global.getPasswordEncoder().encode("0000"), List.of(UserType.STUDENT));
        User professor2 = userService.addUserInternal("professor2", "professor2@trackdev.com", global.getPasswordEncoder().encode("2222"), List.of(UserType.PROFESSOR));
        List<User> enrolledStudents = createDemoStudents();
        enrolledStudents.add(student1);
        // invites to application
        Invite inviteStudent = inviteService.createInvite("student2@trackdev.com", List.of(UserType.STUDENT), admin.getId());
        Invite inviteUpgradeToAdmin = inviteService.createInvite(professor2.getEmail(), List.of(UserType.ADMIN), admin.getId());
        // courses
        Course course = courseService.createCourse("Test course", admin.getId());
        CourseYear courseYear = courseYearService.createCourseYear(course.getId(), 2021, admin.getId());
        for(int i = 2; i <= 10; i++) {
            Invite inviteCourse = courseYearService.createInvite("student" + i + "@trackdev.com", courseYear.getId(), admin.getId());
        }
        inviteAndEnroll(courseYear, enrolledStudents, admin);
        // one course set up
        Group group = groupService.createGroup("1A", courseYear.getId());
        groupService.addMember(group.getId(), student1.getId());
        Iteration iteration = iterationService.create("First iteration", courseYear.getId());
        Sprint sprint = sprintService.create("Sprint 1", iteration.getId(), group.getId());
        Backlog backlog = backlogService.create(group.getId());
        Task task = taskService.create("Task 1", backlog.getId());
        logger.info("Done populating database");
    }

    private List<User> createDemoStudents() {
        List<String> names = Arrays.asList(
            "Blanca", "Said", "Carles", "Ferran", "Joanot", "Hassen", "Malek", "Osman", "Mahomet", "Guillem", "Roc", // mar i cel
            "Hamlet", "Claudius", "Gertrude", "The Ghost", "Polonius", "Ophelia", "Horatio", "Laertes", "Rosencrantz", "Guildenstern", "Fortinbras" // hamlet
        );
        List<User> users = new ArrayList<>();
        for(String name: names) {
            String username = name.toLowerCase(Locale.ROOT).replace(" ", ".");
            String encodedPassword = global.getPasswordEncoder().encode(username + "1234");
            User user = userService.addUserInternal(username, username + "@trackdev.com", encodedPassword, List.of(UserType.STUDENT));
            users.add(user);
        }
        return users;
    }

    private void inviteAndEnroll(CourseYear courseYear, List<User> users, User admin) {
        for(User user: users) {
            Invite inviteCourse = courseYearService.createInvite(user.getEmail(), courseYear.getId(), admin.getId());
            inviteService.acceptInvite(inviteCourse.getId(), user.getId());
        }
    }
}