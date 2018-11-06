package seedu.addressbook.logic;

import static junit.framework.TestCase.assertEquals;
import static seedu.addressbook.common.Messages.MESSAGE_COMMAND_NOT_FOUND;
import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.addressbook.common.Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK;
import static seedu.addressbook.common.Messages.MESSAGE_WRONG_NUMBER_ARGUMENTS;
import static seedu.addressbook.logic.CommandAssertions.assertCommandBehavior;
import static seedu.addressbook.logic.CommandAssertions.assertInvalidIndexBehaviorForCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.account.ListAccountCommand;
import seedu.addressbook.commands.account.LogoutCommand;
import seedu.addressbook.commands.commandresult.MessageType;
import seedu.addressbook.commands.general.ExitCommand;
import seedu.addressbook.commands.general.HelpCommand;
import seedu.addressbook.commands.person.AddCommand;
import seedu.addressbook.commands.person.ClearCommand;
import seedu.addressbook.commands.person.DeleteCommand;
import seedu.addressbook.commands.person.FindCommand;
import seedu.addressbook.commands.person.ListAllCommand;
import seedu.addressbook.commands.person.ListCommand;
import seedu.addressbook.commands.person.ViewAllCommand;
import seedu.addressbook.commands.person.ViewCommand;
import seedu.addressbook.commands.person.ViewSelfCommand;
import seedu.addressbook.commands.privilege.EditPasswordCommand;
import seedu.addressbook.common.Messages;
import seedu.addressbook.common.Pair;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.data.person.details.Address;
import seedu.addressbook.data.person.details.Email;
import seedu.addressbook.data.person.details.Name;
import seedu.addressbook.data.person.details.Phone;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.privilege.Privilege;
import seedu.addressbook.privilege.user.AdminUser;
import seedu.addressbook.storage.StorageFile;
import seedu.addressbook.stubs.StorageStub;

public class LogicTest {

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private AddressBook addressBook;
    private Privilege privilege;
    private StatisticsBook statisticBook;
    private Logic logic;

    @Before
    public void setUp() throws Exception {
        StorageFile saveFile = new StorageFile(saveFolder.newFile("testSaveFile.txt").getPath(),
                saveFolder.newFile("testExamFile.txt").getPath(),
                saveFolder.newFile("testStatisticsFile.txt").getPath());
        StorageStub stubFile = new StorageStub(saveFolder.newFile("testStubFile.txt").getPath(),
                saveFolder.newFile("testStubExamFile.txt").getPath(),
                saveFolder.newFile("testStubStatisticsFile.txt").getPath());

        addressBook = new AddressBook();
        ExamBook examBook = new ExamBook();
        statisticBook = new StatisticsBook();
        // Privilege set to admin to allow all commands.
        // Privilege restrictions are tested separately under PrivilegeTest.
        privilege = new Privilege(new AdminUser());

        saveFile.save(addressBook);
        saveFile.saveExam(examBook);
        saveFile.saveStatistics(statisticBook);
        logic = new Logic(stubFile, addressBook, examBook, statisticBook, privilege);
        CommandAssertions.setData(saveFile, addressBook, logic, examBook, statisticBook);
    }

    private void setUpThreePerson(AddressBook addressBook,
                                  AddressBook expected,
                                  Logic logic,
                                  TestDataHelper.ThreePersons threePersons) throws Exception {
        TestDataHelper helper = new TestDataHelper();
        helper.addToAddressBook(expected, threePersons.getExpected());
        helper.addToAddressBook(addressBook, threePersons.getActual());
        logic.setLastShownList(threePersons.getActual());
    }

    @Test
    public void constructor() {
        //Constructor is called in the setup() method which executes before every test, no need to call it here again.

        //Confirm the last shown list is empty
        assertEquals(Collections.emptyList(), logic.getLastShownList());
    }

    /** Checks if logic's privilege is raised to Admin when calling initPrivilege with AB of isPerm = true.*/
    @Test
    public void initIsPermSuccess() {
        final AddressBook ab = new AddressBook();
        ab.setPermAdmin(true);
        logic.setAddressBook(ab);
        privilege.resetPrivilege();

        logic.initPrivilege();
        assertEquals(new AdminUser(), privilege.getUser());
    }

    @Test
    public void defaultConstructor() throws Exception {
        // Verifies if addressbook.txt, exams.txt, statistics.txt are loadable
        logic = new Logic();
        //Confirm the last shown list is empty
        assertEquals(Collections.emptyList(), logic.getLastShownList());
    }

    @Test
    public void execute_invalid() throws Exception {
        String invalidCommand = "       ";
        assertCommandBehavior(invalidCommand,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE),
                CommandAssertions.TargetType.AB);
    }

    @Test
    public void execute_unknownCommandWord() throws Exception {
        final HelpCommand helpCommand = new HelpCommand();
        helpCommand.setData(addressBook, statisticBook, new ArrayList<>(), privilege);
        String unknownCommand = "uicfhmowqewca";
        assertCommandBehavior(unknownCommand, MESSAGE_COMMAND_NOT_FOUND, HelpCommand.makeHelpManual());
    }

    // Checks if all void commands is rejected if there is a trailing argument.
    @Test
    public void execute_voidCommandsInvalidCommandReturned() throws Exception {
        List<Pair<String, Command>> inputToExpectedOutput = List.of(
                new Pair<>("clear", new ClearCommand()),
                new Pair<>("list", new ListCommand()),
                new Pair<>("listall", new ListAllCommand()),
                new Pair<>("viewself", new ViewSelfCommand()),
                new Pair<>("listacc", new ListAccountCommand()),
                new Pair<>("logout", new LogoutCommand()),
                new Pair<>("help", new HelpCommand()));

        for (Pair<String, Command> inputToOutput: inputToExpectedOutput) {
            assertCommandBehavior(inputToOutput.getFirst() + " garbage",
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, inputToOutput.getSecond().getCommandUsageMessage()),
                    CommandAssertions.TargetType.AB);
        }
    }

    @Test
    public void executeHelp() throws Exception {
        final HelpCommand helpCommand = new HelpCommand();
        helpCommand.setData(addressBook, statisticBook, new ArrayList<>(), privilege);
        assertCommandBehavior("help", HelpCommand.makeHelpManual(), MessageType.OUTPUT);

        privilege.raiseToTutor();
        assertCommandBehavior("help", HelpCommand.makeHelpManual(), MessageType.OUTPUT);

        privilege.raiseToAdmin();
        assertCommandBehavior("help", HelpCommand.makeHelpManual(), MessageType.OUTPUT);
    }

    @Test
    public void executeExit() throws Exception {
        assertCommandBehavior("exit", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeClearSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        // generates the 3 test people and execute the add command
        for (int i = 1; i <= 3; ++i) {
            final Person testPerson = helper.generatePerson(i, true);
            addressBook.addPerson(testPerson);
            logic.execute(helper.generateAddCommand(testPerson));
        }

        assertCommandBehavior("clear",
                ClearCommand.MESSAGE_SUCCESS,
                AddressBook.empty(),
                true,
                Collections.emptyList(),
                true);
    }

    @Test
    public void executeAddInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertCommandBehavior(
                "add wrong args wrong args", expectedMessage,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name 12345 e/valid@email.butNoPhonePrefix a/valid, address", expectedMessage,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/12345 valid@email.butNoPrefix a/valid, address", expectedMessage,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/12345 e/valid@email.butNoAddressPrefix valid, address", expectedMessage,
                CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeAddInvalidPersonData() throws Exception {
        assertCommandBehavior(
                "add []\\[;] p/12345 e/valid@e.mail a/valid, address", Name.MESSAGE_NAME_CONSTRAINTS,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/not_numbers e/valid@e.mail a/valid, address", Phone.MESSAGE_PHONE_CONSTRAINTS,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/12345 e/notAnEmail a/valid, address", Email.MESSAGE_EMAIL_CONSTRAINTS,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/12345 e/valid@e.mail a/valid, address t/invalid_-[.tag", Tag.MESSAGE_TAG_CONSTRAINTS,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/12345 e/valid@e.mail a/#$%#@#What Am I?", Address.MESSAGE_ADDRESS_CONSTRAINTS,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior(
                "add Valid Name p/1234 e/valid@e.mail a/valid, address t/invalid_-[.tag", Tag.MESSAGE_TAG_CONSTRAINTS,
                CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeAddSuccessful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Person toBeAdded = helper.adam();
        AddressBook expected = new AddressBook();
        expected.addPerson(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expected,
                true,
                List.of(toBeAdded),
                true);
    }

    @Test
    public void executeAddDuplicateNotAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Person toBeAdded = helper.adam();
        AddressBook expected = new AddressBook();
        expected.addPerson(toBeAdded);

        // setup starting state
        addressBook.addPerson(toBeAdded); // person already in internal address book

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddCommand(toBeAdded),
                AddCommand.MESSAGE_DUPLICATE_PERSON,
                expected,
                false,
                Collections.emptyList());
    }

    @Test
    public void executeListShowsAllPersons() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = helper.generateAddressBook(false, true);
        List<? extends ReadOnlyPerson> expectedList = expected.getAllPersons().immutableListView();

        // prepare address book state
        helper.addToAddressBook(addressBook, false, true);

        assertCommandBehavior("list",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeListAllShowsAllPersons() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = helper.generateAddressBook(false, true);
        List<? extends ReadOnlyPerson> expectedList = expected.getAllPersons().immutableListView();

        // prepare address book state
        helper.addToAddressBook(addressBook, false, true);

        assertCommandBehavior("listall",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeViewInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertCommandBehavior("view ", expectedMessage, CommandAssertions.TargetType.AB);
        assertCommandBehavior("view arg not number", expectedMessage, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeViewInvalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("view");
    }

    @Test
    public void executeViewOnlyShowsNonPrivate() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, true);
        Person p2 = helper.generatePerson(2, false);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);
        AddressBook expected = helper.generateAddressBook(lastShownList);
        helper.addToAddressBook(addressBook, lastShownList);

        logic.setLastShownList(lastShownList);

        assertCommandBehavior("view 1",
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_DETAILS, p1.getName()),
                p1.getAsTextHidePrivate(),
                expected,
                false,
                lastShownList);

        assertCommandBehavior("view 2",
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_DETAILS, p2.getName()),
                p2.getAsTextHidePrivate(),
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeTryToViewMissingPersonErrorMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, false);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);

        AddressBook expected = new AddressBook();
        expected.addPerson(p2);

        addressBook.addPerson(p2);
        logic.setLastShownList(lastShownList);

        assertCommandBehavior("view 1",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeViewAllInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewAllCommand.MESSAGE_USAGE);
        assertCommandBehavior("viewall ", expectedMessage, CommandAssertions.TargetType.AB);
        assertCommandBehavior("viewall arg not number", expectedMessage, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeViewAllInvalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("viewall");
    }

    @Test
    public void executeViewAllAlsoShowsPrivate() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, true);
        Person p2 = helper.generatePerson(2, false);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);
        AddressBook expected = helper.generateAddressBook(lastShownList);
        helper.addToAddressBook(addressBook, lastShownList);

        logic.setLastShownList(lastShownList);

        assertCommandBehavior("viewall 1",
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_DETAILS, p1.getName()),
                p1.getAsTextShowAll(),
                expected,
                false,
                lastShownList);


        assertCommandBehavior("viewall 2",
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_DETAILS, p2.getName()),
                p2.getAsTextShowAll(),
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeTryToViewAllPersonMissingInAddressBookErrorMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, false);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);

        AddressBook expected = new AddressBook();
        expected.addPerson(p1);

        addressBook.addPerson(p1);
        logic.setLastShownList(lastShownList);

        assertCommandBehavior("viewall 2",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeDeleteInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        assertCommandBehavior("delete ", expectedMessage, CommandAssertions.TargetType.AB);
        assertCommandBehavior("delete arg not number", expectedMessage, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeDeleteInvalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("delete");
    }

    @Test
    public void executeDeleteRemovesCorrectPerson() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p2 = threePersons.getExpectedPerson(2);
        expected.removePerson(p2);
        threePersons.getExpected().remove(p2);

        assertCommandBehavior("delete 2",
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, p2),
                expected,
                true,
                threePersons.getExpected(),
                true);
    }

    @Test
    public void executeDeleteSelfExceptionThrown() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p2 = threePersons.getActualPerson(2);
        privilege.setMyPerson(p2);
        assertCommandBehavior("delete 2",
                DeleteCommand.MESSAGE_DELETING_SELF,
                expected,
                false,
                threePersons.getExpected(),
                true);
    }

    @Test
    public void executeDeleteMissingInAddressBook() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, true);
        Person p3 = helper.generatePerson(3, true);

        List<Person> threePersons = helper.generatePersonList(p1, p2, p3);

        AddressBook expected = helper.generateAddressBook(threePersons);
        expected.removePerson(p2);

        helper.addToAddressBook(addressBook, threePersons);
        addressBook.removePerson(p2);
        logic.setLastShownList(threePersons);

        assertCommandBehavior("delete 2",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                threePersons);
    }

    @Test
    public void executeFindInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("find ", expectedMessage, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeFindOnlyMatchesFullWordsInNames() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person pTarget1 = helper.generatePersonWithName("bla bla KEY bla");
        Person pTarget2 = helper.generatePersonWithName("bla KEY bla bceofeia");
        Person p1 = helper.generatePersonWithName("KE Y");
        Person p2 = helper.generatePersonWithName("KEYKEYKEY sduauo");

        List<Person> fourPersons = helper.generatePersonList(p1, pTarget1, p2, pTarget2);
        AddressBook expected = helper.generateAddressBook(fourPersons);
        List<Person> expectedList = helper.generatePersonList(pTarget1, pTarget2);
        helper.addToAddressBook(addressBook, fourPersons);

        assertCommandBehavior("find KEY",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeFindIsCaseSensitive() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person pTarget1 = helper.generatePersonWithName("bla bla KEY bla");
        Person pTarget2 = helper.generatePersonWithName("bla KEY bla bceofeia");
        Person p1 = helper.generatePersonWithName("key key");
        Person p2 = helper.generatePersonWithName("KEy sduauo");

        List<Person> fourPersons = helper.generatePersonList(p1, pTarget1, p2, pTarget2);
        AddressBook expected = helper.generateAddressBook(fourPersons);
        List<Person> expectedList = helper.generatePersonList(pTarget1, pTarget2);
        helper.addToAddressBook(addressBook, fourPersons);

        assertCommandBehavior("find KEY",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeFindMatchesIfAnyKeywordPresent() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person pTarget1 = helper.generatePersonWithName("bla bla KEY bla");
        Person pTarget2 = helper.generatePersonWithName("bla rAnDoM bla bceofeia");
        Person p1 = helper.generatePersonWithName("key key");
        Person p2 = helper.generatePersonWithName("KEy sduauo");

        List<Person> fourPersons = helper.generatePersonList(p1, pTarget1, p2, pTarget2);
        AddressBook expected = helper.generateAddressBook(fourPersons);
        List<Person> expectedList = helper.generatePersonList(pTarget1, pTarget2);
        helper.addToAddressBook(addressBook, fourPersons);

        assertCommandBehavior("find KEY rAnDoM",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeViewSelfNotLoggedIn() throws Exception {
        assertCommandBehavior("viewself", Messages.MESSAGE_NOT_LOGGED_IN, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeViewSelfSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p2 = threePersons.getActualPerson(2);
        privilege.setMyPerson(p2);
        assertCommandBehavior("viewself",
                String.format(ViewSelfCommand.MESSAGE_VIEW_PERSON_DETAILS, p2.getName()),
                p2.getAsTextShowAll(),
                expected,
                false,
                threePersons.getExpected());
    }

    @Test
    public void executeChangePasswordInvalidArguments() throws Exception {
        final String initialPassword = addressBook.getMasterPassword();
        final String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                EditPasswordCommand.MESSAGE_USAGE);
        assertCommandBehavior("editpw", expectedMessage, CommandAssertions.TargetType.AB);
        assertCommandBehavior("editpw ", expectedMessage, CommandAssertions.TargetType.AB);
        assertEquals(addressBook.getMasterPassword(), initialPassword);
    }

    @Test
    public void executeChangePasswordInvalidArgumentNumber() throws Exception {
        final String initialPassword = addressBook.getMasterPassword();
        final String expectedMessage = MESSAGE_WRONG_NUMBER_ARGUMENTS;
        final int requiredArguments = 2;
        int actualArguments = 1;
        assertCommandBehavior("editpw default_pw",
                String.format(expectedMessage, requiredArguments, actualArguments, EditPasswordCommand.MESSAGE_USAGE),
                CommandAssertions.TargetType.AB);

        actualArguments = 3;
        assertCommandBehavior("editpw default_pw new_pw extra_arg",
                String.format(expectedMessage, requiredArguments, actualArguments,
                        EditPasswordCommand.MESSAGE_USAGE), CommandAssertions.TargetType.AB);
        assertEquals(addressBook.getMasterPassword(), initialPassword);
    }

    @Test
    public void executeChangePasswordWrongPassword() throws Exception {
        final String initialPassword = addressBook.getMasterPassword();
        String expectedMessage = EditPasswordCommand.MESSAGE_WRONG_PASSWORD;
        assertCommandBehavior("editpw wrong_password new_password", expectedMessage,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior("editpw default_password1 new_password", expectedMessage,
                CommandAssertions.TargetType.AB);
        assertCommandBehavior("editpw Default_password new_password", expectedMessage,
                CommandAssertions.TargetType.AB);
        assertEquals(addressBook.getMasterPassword(), initialPassword);
    }

    @Test
    public void executeChangePasswordSameAsOldPassword() throws Exception {
        String expectedMessage = EditPasswordCommand.MESSAGE_SAME_AS_OLD_PASSWORD;
        assertCommandBehavior("editpw default_pw default_pw", expectedMessage, CommandAssertions.TargetType.AB);
        addressBook.setMasterPassword("new_password");
        assertCommandBehavior("editpw new_password new_password", expectedMessage, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeChangePasswordSuccess() throws Exception {
        final String expectedMessage = EditPasswordCommand.MESSAGE_SUCCESS;
        final String commandFormat = "editpw %s %s";
        String oldPassword = addressBook.getMasterPassword();
        String newPassword = "new_password";
        String commandInput = String.format(commandFormat, oldPassword, newPassword);
        assertCommandBehavior(commandInput,
                String.format(expectedMessage, newPassword), CommandAssertions.TargetType.AB);
        assertEquals(addressBook.getMasterPassword(), newPassword);

        oldPassword = addressBook.getMasterPassword();
        newPassword = "another_new_password";
        commandInput = String.format(commandFormat, oldPassword, newPassword);
        assertCommandBehavior(commandInput,
                String.format(expectedMessage, newPassword), CommandAssertions.TargetType.AB);
        assertEquals(addressBook.getMasterPassword(), newPassword);
    }
}
