package seedu.addressbook.logic;

import static junit.framework.TestCase.assertEquals;
import static seedu.addressbook.common.Messages.MESSAGE_INVALID_EXAM_DISPLAYED_INDEX;
import static seedu.addressbook.common.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;

import java.util.Collections;
import java.util.List;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.commandresult.CommandResult;
import seedu.addressbook.commands.commandresult.MessageType;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.person.Exam;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyExam;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.stubs.StorageStub;

/**
 * This class holds the commonly used assertions of commands.
 * */
public class CommandAssertions {
    private static StorageStub saveFile;
    private static AddressBook addressBook;
    private static Logic logic;
    private static ExamBook examBook;
    private static StatisticsBook statisticsBook;

    /**
     * Enum used to indicate which file is the command targeting to check
     * */
    public enum TargetType {
        PERSONS,
        EXAMS,
        STATS
    }

    public static void setData(StorageStub saveFile, AddressBook addressBook, Logic logic) {
        CommandAssertions.saveFile = saveFile;
        CommandAssertions.addressBook = addressBook;
        CommandAssertions.saveFile.save(addressBook);
        CommandAssertions.logic = logic;
    }

    public static void setData(StorageStub saveFile, AddressBook addressBook, Logic logic, ExamBook examBook,
                               StatisticsBook statisticsBook) {
        setData(saveFile, addressBook, logic);
        CommandAssertions.examBook = examBook;
        CommandAssertions.saveFile.saveExam(examBook);
        CommandAssertions.statisticsBook = statisticsBook;
        CommandAssertions.saveFile.saveStatistics(statisticsBook);
    }

    /**
     * Executes the command and confirms that the result messages are correct.
     * Both the 'book' and the 'last shown list' are expected to be empty.
     * @see #assertCommandBehavior(String, String, AddressBook, boolean, List)
     * @see #assertCommandBehavior(String, String, ExamBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand, String expectedMessage,
                                             TargetType targetType) throws Exception {
        if (targetType.equals(TargetType.PERSONS)) {
            assertCommandBehavior(inputCommand, expectedMessage, AddressBook.empty(),
                    false, Collections.emptyList());
        } else if (targetType.equals(TargetType.EXAMS)) {
            assertCommandBehavior(inputCommand, expectedMessage, ExamBook.empty(),
                    false, Collections.emptyList());
        }
    }

    /**
     * Executes the command and confirms that the result messages are correct.
     * The `exam book` and the `last shown list` are given.
     * @see #assertCommandBehavior(String, String, ExamBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand, String expectedStatusMessage,
                                             ExamBook exambook, List<ReadOnlyExam> lastShownList) throws Exception {
        assertCommandBehavior(inputCommand, expectedStatusMessage, exambook,
                false, lastShownList);
    }

    /**
     * Executes the command and confirms that the status and output messages are correct.
     * Both the 'address book' and the 'last shown list' are expected to be empty.
     * @see #assertCommandBehavior(String, String, String, AddressBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             String expectedOutputMessage) throws Exception {
        assertCommandBehavior(inputCommand, expectedStatusMessage, expectedOutputMessage,
                AddressBook.empty(), false, Collections.emptyList());
    }

    /**
     * Executes the command and confirms that the result messages are correct.
     * Both the 'address book' and the 'last shown list' are expected to be empty.
     * @param messageType specify which console the given message is supposed to be written to
     * @see #assertCommandBehavior(String, String, AddressBook, boolean, List)
     * @see #assertCommandBehavior(String, String, String, AddressBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedMessage,
                                             MessageType messageType) throws Exception {
        if (messageType.equals(MessageType.STATUS)) {
            assertCommandBehavior(inputCommand, expectedMessage, AddressBook.empty(),
                    false, Collections.emptyList());
        } else if (messageType.equals(MessageType.OUTPUT)) {
            assertCommandBehavior(inputCommand, "", expectedMessage, AddressBook.empty(),
                    false, Collections.emptyList());
        }
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * @param messageType specify which console the given message is supposed to be written to
     * @see #assertCommandBehavior(String, String, AddressBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedMessage,
                                             MessageType messageType,
                                             AddressBook expectedAddressBook,
                                             boolean isRelevantPersonsExpected,
                                             List<? extends ReadOnlyPerson> lastShownList) throws Exception {
        if (messageType.equals(MessageType.STATUS)) {
            assertCommandBehavior(inputCommand,
                    expectedMessage,
                    "",
                    expectedAddressBook,
                    isRelevantPersonsExpected,
                    lastShownList);
        } else if (messageType.equals(MessageType.OUTPUT)) {
            assertCommandBehavior(inputCommand,
                    "",
                    expectedMessage,
                    expectedAddressBook,
                    isRelevantPersonsExpected,
                    lastShownList);
        }
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * sets the expectedOutputMessage to be empty.
     * @see #assertCommandBehavior(String, String, String, AddressBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand,
                                      String expectedStatusMessage,
                                      AddressBook expectedAddressBook,
                                      boolean isRelevantPersonsExpected,
                                      List<? extends ReadOnlyPerson> lastShownList) throws Exception {
        assertCommandBehavior(inputCommand, expectedStatusMessage, "", expectedAddressBook,
                isRelevantPersonsExpected, lastShownList);
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * also confirms that the following two parts of the Logic object's state are as expected:<br>
     *      - the internal address book data are same as those in the {@code expectedAddressBook} <br>
     *      - the internal 'last shown list' matches the {@code lastShownList} <br>
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             String expectedOutputMessage,
                                             AddressBook expectedAddressBook,
                                             boolean isRelevantPersonsExpected,
                                             List<? extends ReadOnlyPerson> lastShownList) throws Exception {
        //Execute the command
        CommandResult r = logic.execute(inputCommand);

        //Confirm the result contains the right data
        assertEquals(expectedOutputMessage, r.getOutputConsoleMessage());
        assertEquals(expectedStatusMessage, r.getStatusConsoleMessage());
        assertEquals(r.getRelevantPersons().isPresent(), isRelevantPersonsExpected);
        if (isRelevantPersonsExpected) {
            assertEquals(lastShownList, r.getRelevantPersons().get());
        }

        //Confirm the state of data is as expected
        assertEquals(expectedAddressBook, addressBook);
        assertEquals(lastShownList, logic.getLastShownList());
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * sets the expectedOutputMessage to be empty.
     * @see #assertCommandBehavior(String, String, String, ExamBook, boolean, List)
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             ExamBook expectedExamBook,
                                             boolean isRelevantExamsExpected,
                                             List<? extends ReadOnlyExam> lastShownList) throws Exception {
        assertCommandBehavior(inputCommand, expectedStatusMessage, "", expectedExamBook,
                isRelevantExamsExpected, lastShownList);
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * also confirms that the following two parts of the Logic object's state are as expected:<br>
     *      - the internal exam book data are same as those in the {@code expectedExamBook} <br>
     *      - the internal 'last shown list' matches the {@code lastShownList} <br>
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             String expectedOutputMessage,
                                             ExamBook expectedExamBook,
                                             boolean isRelevantExamsExpected,
                                             List<? extends ReadOnlyExam> lastShownList) throws Exception {
        //Execute the command
        CommandResult r = logic.execute(inputCommand);

        //Confirm the result contains the right data
        assertEquals(expectedStatusMessage, r.getStatusConsoleMessage());
        assertEquals(expectedOutputMessage, r.getOutputConsoleMessage());
        assertEquals(r.getRelevantExams().isPresent(), isRelevantExamsExpected);
        if (isRelevantExamsExpected) {
            assertEquals(lastShownList, r.getRelevantExams().get());
        }
        //Confirm the state of data is as expected
        assertEquals(expectedExamBook, examBook);
        assertEquals(lastShownList, logic.getLastShownExamList());
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * also confirms that the following part of the Logic object's state are as expected:<br>
     *      - the internal statistics book data are same as those in the {@code expectedStatisticsBook} <br>
     *
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             StatisticsBook expectedStatisticsBook,
                                             boolean isRelevantStatsticsExpected) throws Exception {
        //Execute the command
        CommandResult r = logic.execute(inputCommand);

        //Confirm the result contains the right data
        assertEquals(expectedStatusMessage, r.getStatusConsoleMessage());

        //Confirm the state of data is as expected
        assertEquals(expectedStatisticsBook, statisticsBook);
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * Assumes the command writes to file and the expectedOutputMessage to be empty
     * @see #assertCommandBehavior(String, String, String, AddressBook, ExamBook, boolean, boolean, List, List)
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             AddressBook expectedAddressBook,
                                             ExamBook expectedExamBook,
                                             boolean isRelevantPersonsExpected,
                                             boolean isRelevantExamsExpected,
                                             List<? extends ReadOnlyPerson> lastShownList,
                                             List<? extends ReadOnlyExam> lastShownExamList) throws Exception {
        assertCommandBehavior(inputCommand,
                expectedStatusMessage,
                "",
                expectedAddressBook,
                expectedExamBook,
                isRelevantPersonsExpected,
                isRelevantExamsExpected,
                lastShownList,
                lastShownExamList);
    }

    /**
     * Executes the command and confirms that the result messages are correct and
     * also confirms that the following four parts of the Logic object's state are as expected:<br>
     *     - the internal address book data are same as those in the {@code expectedAddressBook} <br>
     *     - the internal exam book data are same as those in the {@code expectedExamBook} <br>
     *     - the internal 'last shown list' matches the {@code lastShownList} <br>
     *     - the internal 'last shown exam list' matches the {@code lastShownExamList} <br>
     */
    public static void assertCommandBehavior(String inputCommand,
                                             String expectedStatusMessage,
                                             String expectedOutputMessage,
                                             AddressBook expectedAddressBook,
                                             ExamBook expectedExamBook,
                                             boolean isRelevantPersonsExpected,
                                             boolean isRelevantExamsExpected,
                                             List<? extends ReadOnlyPerson> lastShownList,
                                             List<? extends ReadOnlyExam> lastShownExamList) throws Exception {
        //Execute the command
        CommandResult r = logic.execute(inputCommand);
        //Confirm the result contains the right data
        assertEquals(expectedStatusMessage, r.getStatusConsoleMessage());
        assertEquals(expectedOutputMessage, r.getOutputConsoleMessage());
        assertEquals(r.getRelevantPersons().isPresent(), isRelevantPersonsExpected);
        assertEquals(r.getRelevantExams().isPresent(), isRelevantExamsExpected);
        if (isRelevantPersonsExpected) {
            assertEquals(lastShownList, r.getRelevantPersons().get());
        }
        if (isRelevantExamsExpected) {
            assertEquals(lastShownExamList, r.getRelevantExams().get());
        }

        //Confirm the state of data is as expected
        assertEquals(expectedExamBook, examBook);
        assertEquals(expectedAddressBook, addressBook);
    }

    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single object in the last shown list, using visible index.
     * Used for commands in the form of COMMAND_WORD INDEX
     * @param commandWord to test assuming it targets a single object in the last shown list based on visible index.
     * @param targetType to check which list the command is targeting
     */
    public static void assertInvalidIndexBehaviorForCommand(String commandWord,
                                                            TargetType targetType) throws Exception {
        final String[] commands = {commandWord + " 0", commandWord + " -1", commandWord + " 3"};
        assertInvalidIndexBehaviour(commands, targetType);
    }

    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single object in the last shown list, using visible index.
     * Used for commands in the form of COMMAND_WORD PREFIX INDEX SUFFIX
     * PREFIX and SUFFIX can be empty
     * @param commandWord of the command.
     * @param prefix containing required information to enter before the INDEX.
     * @param suffix containing required information to enter after the INDEX.
     * @param targetType to check which list the command is targeting
     */
    public static void assertInvalidIndexBehaviorForCommand(String commandWord, String prefix, String suffix,
                                                            TargetType targetType) throws Exception {
        final String[] commands = {String.format("%s %s 0 %s", commandWord, prefix, suffix),
                String.format("%s %s -1 %s", commandWord, prefix, suffix),
                String.format("%s %s 3 %s", commandWord, prefix, suffix)};
        assertInvalidIndexBehaviour(commands, targetType);
    }

    /**
     * Confirms the 'invalid argument index number behaviour' for the given command array
     * targeting a single object in the last shown list, using visible index.
     *
     * @param commands to test assuming it targets a single person in the last shown list based on visible index.
     * @param targetType to check which list the command is targeting
     */
    private static void assertInvalidIndexBehaviour(String[] commands, TargetType targetType) throws Exception {
        TestDataHelper helper = new TestDataHelper();
        String errorMessage;

        if (targetType.equals(TargetType.PERSONS)) {
            List<Person> lastShownList = helper.generatePersonList(false, true);

            logic.setLastShownList(lastShownList);

            errorMessage = MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
            for (String command: commands) {
                assertCommandBehavior(command, errorMessage,
                        AddressBook.empty(), false, lastShownList);
            }
        } else if (targetType.equals(TargetType.EXAMS)) {
            List<Exam> lastShownList = helper.generateExamList(false, true);

            logic.setLastShownExamList(lastShownList);

            errorMessage = MESSAGE_INVALID_EXAM_DISPLAYED_INDEX;
            for (String command: commands) {
                assertCommandBehavior(command, errorMessage,
                        ExamBook.empty(), false, lastShownList);
            }
        }
    }
}
