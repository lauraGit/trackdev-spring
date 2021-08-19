package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = TaskActiveSprintChange.CHANGE_TYPE_NAME)
public class TaskActiveSprintChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "active_sprint_change";

    public TaskActiveSprintChange() {}

    public TaskActiveSprintChange(User author, Task task, Sprint activeSprint) {
        super(author, task);
        this.activeSprint = activeSprint;
    }

    @ManyToOne
    @JoinColumn(
        name = "activeSprintId",
        foreignKey = @ForeignKey(
            name= "active_sprint_foreign_key_cascade",
            foreignKeyDefinition = "FOREIGN KEY (`activeSprintId`) REFERENCES `sprints` (`id`) ON DELETE SET NULL"))
    private Sprint activeSprint;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Sprint getActiveSprint() { return this.activeSprint; }
}