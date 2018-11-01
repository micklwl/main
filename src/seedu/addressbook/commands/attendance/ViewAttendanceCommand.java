package seedu.addressbook.commands.attendance;

import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.commandresult.CommandResult;
import seedu.addressbook.common.Messages;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.UniquePersonList;

/**
 *  Lists down the dates where the person's attendance has been taken.
 */
public class ViewAttendanceCommand extends Command {

    public static final String COMMAND_WORD = "viewAtten";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ":\n"
            + "Views the attendance of a student. \n"
            + "Parameters: indexOfStudent \n"
            + "\tExample: " + COMMAND_WORD + " " + "1";

    public static final String MESSAGE_SUCCESS = "Attendance for student, ";

    // Constructor
    public ViewAttendanceCommand(int targetIndex) {
        super(targetIndex);
    }

    /**
     * Constructor used for Privileges
     * Command constructed has no functionality
     * */
    public ViewAttendanceCommand() {
        // Does nothing
    }

    @Override
    public CommandResult execute() {
        try {
            Person person = addressBook.findPerson(getTargetReadOnlyPerson());
            final String output = person.viewAttendanceMethod();
            return new CommandResult(String.format(MESSAGE_SUCCESS) + person.getName() + ":\n" + output);

        } catch (IndexOutOfBoundsException ie) {
            return new CommandResult(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        } catch (UniquePersonList.PersonNotFoundException pnfe) {
            return new CommandResult(Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK);
        }
    }

    @Override
    public String getCommandUsageMessage() {
        return MESSAGE_USAGE;
    }
}