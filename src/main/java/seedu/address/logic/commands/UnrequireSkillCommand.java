package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILL;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_GROUPS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.coursemate.Name;
import seedu.address.model.group.Group;
import seedu.address.model.group.exceptions.GroupNotFoundException;
import seedu.address.model.skill.Skill;

/**
 * Removes required skill(s) to a preexisting group.
 */
public class UnrequireSkillCommand extends Command {
    public static final String COMMAND_WORD = "unrequire-skill";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes required skills to an already existing group.\n"
            + "Parameters: GROUP_NAME (group must exist) "
            + PREFIX_SKILL + " SKILL "
            + "[" + PREFIX_SKILL + " SKILL" + "]...\n"
            + "Example: " + COMMAND_WORD + " CS2103T GROUP "
            + PREFIX_SKILL + " Python "
            + PREFIX_SKILL + " Java";

    public static final String MESSAGE_SUCCESSFULLY_ADDED = "Group successfully modified, Name: %1$s\n"
            + "%2$s skill(s) are now required for the group!\n";

    public static final String MESSAGE_MISSING_SKILLS = "The following skills couldn't be found: ";

    private final Name groupName;
    private final Set<Skill> skillSet;

    /**
     * Basic constructor for {@code UnrequireSkillCommand}.
     */
    public UnrequireSkillCommand(Name groupName, Set<Skill> skillSet) {
        requireAllNonNull(groupName, skillSet);
        this.groupName = groupName;
        this.skillSet = skillSet;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Group toModify;
        try {
            toModify = model.findGroup(groupName);
        } catch (GroupNotFoundException e) {
            throw new CommandException(Messages.MESSAGE_INVALID_GROUP_NAME);
        }

        int removedCounter = 0;
        List<Skill> notFoundSkills = new ArrayList<>();

        Set<Skill> modifiedSkillSet = new HashSet<>(toModify.getSkills());
        for (Skill skill: skillSet) {
            if (modifiedSkillSet.contains(skill)) {
                modifiedSkillSet.remove(skill);
                removedCounter += 1;
            } else {
                notFoundSkills.add(skill);
            }
        }
        Group modifiedGroup = new Group(
                toModify.getName(),
                toModify.asUnmodifiableObservableList(),
                modifiedSkillSet,
                toModify.getTelegramChat());

        model.setGroup(toModify, modifiedGroup);
        model.updateFilteredGroupList(PREDICATE_SHOW_ALL_GROUPS);

        String stringResult = "";

        if (removedCounter > 0) {
            stringResult += String.format(MESSAGE_SUCCESSFULLY_ADDED,
                    groupName, modifiedGroup.getSkills().size());
        }

        if (!notFoundSkills.isEmpty()) {
            stringResult += MESSAGE_MISSING_SKILLS + notFoundSkills
                    .stream()
                    .map(Skill::toString)
                    .collect(Collectors.joining(", "));
        }

        return new CommandResult(stringResult, false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof UnrequireSkillCommand)) {
            return false;
        }

        UnrequireSkillCommand otherUnrequireSkillCommand = (UnrequireSkillCommand) other;
        return otherUnrequireSkillCommand.groupName.equals(groupName)
                && otherUnrequireSkillCommand.skillSet.equals(skillSet);
    }
}
