package org.udg.trackdev.spring.entity.sprintchanges;

import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.changes.EntityLogChange;

import javax.persistence.*;

@Entity
@Table(name = "sprint_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@AssociationOverride(
    name="entity",
    joinColumns = @JoinColumn(name = "entityId"),
    foreignKey = @ForeignKey(
        name="sprint_foreign_key_cascade",
        foreignKeyDefinition = "FOREIGN KEY (`entityId`) REFERENCES `sprints` (`id`) ON DELETE CASCADE")
)
public abstract class SprintChange extends EntityLogChange<Sprint> {

    public SprintChange() { }

    public SprintChange(User author, Sprint sprint) {
        super(author, sprint);
    }
}