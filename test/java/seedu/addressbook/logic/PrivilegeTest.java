package seedu.addressbook.logic;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.addressbook.common.Messages.MESSAGE_INSUFFICIENT_PRIVILEGE;
import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.addressbook.common.Messages.MESSAGE_WRONG_NUMBER_ARGUMENTS;
import static seedu.addressbook.logic.CommandAssertions.TargetType;
import static seedu.addressbook.logic.CommandAssertions.assertCommandBehavior;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.commandresult.MessageType;
import seedu.addressbook.commands.privilege.RaisePrivilegeCommand;
import seedu.addressbook.commands.privilege.SetPermanentAdminCommand;
import seedu.addressbook.commands.privilege.ViewPrivilegeCommand;
import seedu.addressbook.common.Pair;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.parser.Parser;
import seedu.addressbook.privilege.Privilege;
import seedu.addressbook.privilege.user.AdminUser;
import seedu.addressbook.privilege.user.BasicUser;
import seedu.addressbook.privilege.user.User;
import seedu.addressbook.stubs.StorageStub;

public class PrivilegeTest {

    /**
     * This tests for Commands that affects or depends on Privilege
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();
    private AddressBook addressBook = new AddressBook();
    private Privilege privilege = new Privilege();

    @Before
    public void setUp() throws Exception {
        StorageStub stubFile;

        ExamBook examBook = new ExamBook();
        StatisticsBook statisticsBook = new StatisticsBook();

        stubFile = new StorageStub(saveFolder.newFile("testStubFile.txt").getPath(),
                saveFolder.newFile("testStubExamFile.txt").getPath(),
                saveFolder.newFile("testStubStatisticsFile.txt").getPath());


        Logic logic = new Logic(stubFile, addressBook, examBook, statisticsBook, privilege);
        CommandAssertions.setData(stubFile, addressBook, logic);
    }

    @Test
    public void executeSayNotLoggedIn() throws Exception {
        final String feedbackFormat = ViewPrivilegeCommand.MESSAGE_NOT_LOGGED_IN
                + ViewPrivilegeCommand.MESSAGE_PRIVILEGE_FORMAT;
        assertCommandBehavior("viewpri",
                        String.format(feedbackFormat, User.PrivilegeLevel.Basic.toString()), MessageType.OUTPUT);

        privilege.raiseToTutor();
        assertCommandBehavior("viewpri",
                        String.format(feedbackFormat, User.PrivilegeLevel.Tutor.toString()), MessageType.OUTPUT);

        privilege.raiseToAdmin();
        assertCommandBehavior("viewpri",
                        String.format(feedbackFormat, User.PrivilegeLevel.Admin.toString()), MessageType.OUTPUT);
    }

    @Test
    public void executeSayLoggedIn() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        final Person testPerson = helper.adam();
        final String feedbackFormat = String.format(ViewPrivilegeCommand.MESSAGE_LOGGED_IN, testPerson.getName())
                + ViewPrivilegeCommand.MESSAGE_PRIVILEGE_FORMAT;

        privilege.setMyPerson(testPerson);
        assertCommandBehavior("viewpri",
                String.format(feedbackFormat, User.PrivilegeLevel.Basic.toString()), MessageType.OUTPUT);

        privilege.raiseToTutor();
        assertCommandBehavior("viewpri",
                String.format(feedbackFormat, User.PrivilegeLevel.Tutor.toString()), MessageType.OUTPUT);

        privilege.raiseToAdmin();
        assertCommandBehavior("viewpri",
                String.format(feedbackFormat, User.PrivilegeLevel.Admin.toString()), MessageType.OUTPUT);
    }

    @Test
    public void assertDefaultPassword() {
        final String defaultPassword = "default_pw";
        assertEquals(addressBook.getMasterPassword(), defaultPassword);
    }

    @Test
    public void executeRaisePrivilegeInvalidArg() throws Exception {
        final String[] inputs = {"raise", "raise "};

        final String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                RaisePrivilegeCommand.MESSAGE_USAGE);

        for (String input: inputs) {
            assertCommandBehavior(input, expectedMessage, TargetType.PERSONS);
        }
        assertEquals(privilege.getUser(), new BasicUser());
    }

    @Test
    public void executeRaisePrivilegeWrongNumberOfArg() throws Exception {
        // First value of the pair represents String input
        // Second value of the pair represents number of arguments supplied, as an Integer.
        List<Pair<String, Integer>> inputToExpectedOutput = new ArrayList<>();
        inputToExpectedOutput.add(new Pair<>("raise arg1 arg2 ", 2));
        inputToExpectedOutput.add(new Pair<>("raise arg1 arg2 arg3 ", 3));

        final int requiredArgument = 1;

        for (Pair<String, Integer> inputToOutput: inputToExpectedOutput) {
            final String expectedMessage = String.format(MESSAGE_WRONG_NUMBER_ARGUMENTS, requiredArgument,
                    inputToOutput.getSecond(), RaisePrivilegeCommand.MESSAGE_USAGE);
            assertCommandBehavior(inputToOutput.getFirst(),
                    expectedMessage, TargetType.PERSONS);
        }
        assertEquals(privilege.getUser(), new BasicUser());
    }

    @Test
    public void executeRaisePrivilegeWrongPassword() throws Exception {
        String expectedMessage = RaisePrivilegeCommand.MESSAGE_WRONG_PASSWORD;
        assertCommandBehavior("raise wrong_password", expectedMessage,
                TargetType.PERSONS);
        assertEquals(privilege.getUser(), new BasicUser());
    }

    @Test
    public void executeRaisePrivilegeLoggedIn() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person person = helper.adam();
        privilege.setMyPerson(person);
        String expectedMessage = String.format(RaisePrivilegeCommand.MESSAGE_LOGGED_IN, person.getName());
        assertCommandBehavior("raise default_pw", expectedMessage,
                TargetType.PERSONS);
        assertEquals(privilege.getUser(), new BasicUser());
    }

    @Test
    public void executeRaisePrivilegeSuccessDefaultPassword() throws Exception {
        String defaultPassword = AddressBook.DEFAULT_MASTER_PASSWORD;
        assertCommandBehavior("raise " + defaultPassword,
                String.format(RaisePrivilegeCommand.MESSAGE_SUCCESS, new AdminUser().getPrivilegeLevelAsString()),
                TargetType.PERSONS);
        assertEquals(privilege.getUser(), new AdminUser());
    }

    @Test
    public void executeRaisePrivilegeSuccessChangedPassword() throws Exception {
        addressBook.setMasterPassword("new_Password");
        assertCommandBehavior("raise new_Password",
                String.format(RaisePrivilegeCommand.MESSAGE_SUCCESS, new AdminUser().getPrivilegeLevelAsString()),
                TargetType.PERSONS);
        assertEquals(privilege.getUser(), new AdminUser());
    }

    @Test
    public void executeSetPermInvalidCommand() throws Exception {
        privilege.raiseToAdmin();
        assertCommandBehavior("perm",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        SetPermanentAdminCommand.MESSAGE_USAGE), TargetType.PERSONS);
        assertFalse(addressBook.isPermAdmin());
    }

    @Test
    public void executeSetPermInvalidDesiredState() throws Exception {
        privilege.raiseToAdmin();
        assertCommandBehavior("perm wut",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        SetPermanentAdminCommand.MESSAGE_USAGE), TargetType.PERSONS);
        assertFalse(addressBook.isPermAdmin());
    }

    @Test
    public void executeSetPermSuccess() throws Exception {
        privilege.raiseToAdmin();
        assertCommandBehavior("perm true",
                String.format(SetPermanentAdminCommand.MESSAGE_SUCCESS, ""), TargetType.PERSONS);
        assertTrue(addressBook.isPermAdmin());

        assertCommandBehavior("perm false",
                String.format(SetPermanentAdminCommand.MESSAGE_SUCCESS, " not"), TargetType.PERSONS);
        assertFalse(addressBook.isPermAdmin());
    }

    @Test
    public void executeAdminCommandsInsufficientPrivilege() throws Exception {
        final String[] inputs = {"clear", "editpw default_pw new_pw",
            "add Valid Name p/12345 e/valid@e.mail a/valid, address ",
            "delete 1"};
        assertCommandsInsufficientPrivilege(inputs);
        privilege.raiseToTutor();
        assertCommandsInsufficientPrivilege(inputs);
    }

    @Test
    public void executeTutorCommandsInsufficientPrivilege() throws Exception {
        final String[] inputs = {"viewall 1"};
        assertCommandsInsufficientPrivilege(inputs);
    }

    /** Asserts that the given command strings cannot be executed using the current privilege level*/
    private void assertCommandsInsufficientPrivilege(String[] inputs) throws Exception {
        Parser parser = new Parser();
        for (String input: inputs) {
            Command command = parser.parseCommand(input);
            assertCommandBehavior(input, String.format(MESSAGE_INSUFFICIENT_PRIVILEGE,
                    privilege.getRequiredPrivilegeAsString(command), privilege.getLevelAsString()),
                    TargetType.PERSONS);
        }
    }
}
