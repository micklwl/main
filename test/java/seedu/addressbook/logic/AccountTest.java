package seedu.addressbook.logic;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static seedu.addressbook.commands.account.AddAccountCommand.MESSAGE_INVALID_PRIVILEGE;
import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.addressbook.common.Messages.MESSAGE_NOT_LOGGED_IN;
import static seedu.addressbook.common.Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK;
import static seedu.addressbook.common.Messages.MESSAGE_WRONG_NUMBER_ARGUMENTS;
import static seedu.addressbook.logic.CommandAssertions.TargetType;
import static seedu.addressbook.logic.CommandAssertions.assertCommandBehavior;
import static seedu.addressbook.logic.CommandAssertions.assertInvalidIndexBehaviorForCommand;
import static seedu.addressbook.privilege.Privilege.PRIVILEGE_CONSTRAINTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.account.AddAccountCommand;
import seedu.addressbook.commands.account.DeleteAccountCommand;
import seedu.addressbook.commands.account.LoginCommand;
import seedu.addressbook.commands.account.LogoutCommand;
import seedu.addressbook.commands.person.ClearCommand;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.account.Account;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.privilege.Privilege;
import seedu.addressbook.privilege.user.AdminUser;
import seedu.addressbook.privilege.user.BasicUser;
import seedu.addressbook.privilege.user.TutorUser;
import seedu.addressbook.privilege.user.User.PrivilegeLevel;
import seedu.addressbook.stubs.StorageStub;

public class AccountTest {

    /**
     * This tests for Commands that affects or depends on Account
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private AddressBook addressBook = new AddressBook();
    private Privilege privilege;
    private Logic logic;

    @Before
    public void setUp() throws Exception {
        StorageStub stubFile;
        ExamBook examBook = new ExamBook();
        StatisticsBook statisticsBook = new StatisticsBook();

        stubFile = new StorageStub(saveFolder.newFile("testStubFile.txt").getPath(),
                saveFolder.newFile("testStubExamFile.txt").getPath(),
                saveFolder.newFile("testStubStatisticsFile.txt").getPath());

        privilege = new Privilege(new AdminUser());
        logic = new Logic(stubFile, addressBook, examBook, statisticsBook, privilege);
        CommandAssertions.setData(stubFile, addressBook, logic);
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
    public void executeAddAccountInvalidArgument() throws Exception {
        assertCommandBehavior("addacc",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAccountCommand.MESSAGE_USAGE),
                TargetType.PERSONS);
    }

    @Test
    public void executeAddAccountInvalidArgumentNumber() throws Exception {
        final String[] inputs = {"addacc user password TUTOR",
            "addacc 2 password TUTOR",
            "addacc 2 username TUTOR",
            "addacc 2 username password",
            "addacc 2 username password TUTOR TrailingArgument"};
        for (String input : inputs) {
            assertCommandBehavior(input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAccountCommand.MESSAGE_USAGE),
                    TargetType.PERSONS);
        }
    }

    @Test
    public void executeAddAccountInvalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("addacc", "", "username password BASIC",
                TargetType.PERSONS);
    }

    @Test
    public void executeAddAccountTargetPersonHasAccount() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p1 = threePersons.getActualPerson(1);
        p1.setAccount(new Account("SomeAccount", "password", "Basic"));
        final Person expectedP1 = threePersons.getExpectedPerson(1);
        expectedP1.setAccount(new Account("SomeAccount", "password", "Basic"));

        final String[] inputs = {"addacc 1 user password Basic",
            "addacc 1 user password TUTOR",
            "addacc 1 user password admin"};
        for (String input : inputs) {
            assertCommandBehavior(input,
                    AddAccountCommand.MESSAGE_PERSON_HAS_ACCOUNT,
                    expected,
                    false,
                    threePersons.getExpected());
        }
    }

    @Test
    public void executeAddAccountPersonMissing() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p2 = threePersons.getActualPerson(2);
        addressBook.removePerson(p2);
        expected.removePerson(p2);
        assertCommandBehavior("addacc 2 user password basic",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                threePersons.getExpected());
    }

    @Test
    public void executeAddAccountInvalidPrivilegeArgument() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final String[] inputPrivileges = {"SUPERMAN", "BasicUser", "Admins"};
        for (String inputPrivilege : inputPrivileges) {
            final String inputFormat = "addacc 2 user password %s";
            final String input = String.format(inputFormat, inputPrivilege);
            assertCommandBehavior(input,
                    String.format(MESSAGE_INVALID_PRIVILEGE, inputPrivilege, PRIVILEGE_CONSTRAINTS),
                    expected,
                    false,
                    threePersons.getExpected());
        }
    }

    @Test
    public void executeAddAccountDuplicateUsername() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p1 = threePersons.getActualPerson(1);
        p1.setAccount(new Account("takenUserName", "password", "Tutor"));
        final Person expectP1 = threePersons.getExpectedPerson(1);
        expectP1.setAccount(new Account("takenUserName", "password", "Tutor"));

        final String[] inputs = {"addacc 2 takenUserName password TUTOR",
            "addacc 2 takenUserName AnotherPassword Basic",
            "addacc 3 takenUserName password admin"};
        for (String input : inputs) {
            assertCommandBehavior(input,
                    AddAccountCommand.MESSAGE_USERNAME_TAKEN,
                    expected,
                    false,
                    threePersons.getExpected());
        }
    }


    @Test
    public void executeAddAccountSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person expectedP1 = threePersons.getExpectedPerson(1);
        expectedP1.setAccount(new Account("user", "password", "Basic"));

        assertCommandBehavior("addacc 1 user password BASIC",
                String.format(AddAccountCommand.MESSAGE_ADD_ACCOUNT_PERSON_SUCCESS, "Person 1"),
                expected,
                true,
                threePersons.getExpected());

        final Person expectedP2 = threePersons.getExpectedPerson(2);
        expectedP2.setAccount(new Account("user2", "password2", "Admin"));
        assertCommandBehavior("addacc 2 user2 password2 admin",
                String.format(AddAccountCommand.MESSAGE_ADD_ACCOUNT_PERSON_SUCCESS, "Person 2"),
                expected,
                true,
                threePersons.getExpected());

        final Person expectedP3 = threePersons.getExpectedPerson(3);
        expectedP3.setAccount(new Account("user3", "password3", "Tutor"));
        assertCommandBehavior("addacc 3 user3 password3 Tutor",
                String.format(AddAccountCommand.MESSAGE_ADD_ACCOUNT_PERSON_SUCCESS, "Person 3"),
                expected,
                true,
                threePersons.getExpected());
    }

    @Test
    public void executeDeleteAccountInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAccountCommand.MESSAGE_USAGE);
        assertCommandBehavior("delacc ", expectedMessage, TargetType.PERSONS);
        assertCommandBehavior("delacc arg not number", expectedMessage, TargetType.PERSONS);
    }

    @Test
    public void executeDeleteAccountInvalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("delacc", TargetType.PERSONS);
    }

    @Test
    public void executeDeleteAccountSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        threePersons.setBothPersons(3, new Account("user3", "pw3", "Admin"));

        final Person p2 = threePersons.getActualPerson(2);
        p2.setAccount(new Account("user2", "pw2", "basic"));

        assertCommandBehavior("delacc 2",
                String.format(DeleteAccountCommand.MESSAGE_DELETE_ACCOUNT_PERSON_SUCCESS, p2.getName()),
                expected,
                true,
                threePersons.getExpected());
    }

    @Test
    public void executeDeleteAccountPersonMissing() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p2 = threePersons.getActualPerson(2);
        final Person expectedP2 = threePersons.getExpectedPerson(2);
        addressBook.removePerson(p2);
        expected.removePerson(expectedP2);

        assertCommandBehavior("delacc 2",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                threePersons.getExpected());
    }

    @Test
    public void executeDeleteAccountTargetHasNoAccount() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);
        assertCommandBehavior("delacc 2",
                DeleteAccountCommand.MESSAGE_PERSON_ACCOUNT_ABSENT,
                expected,
                false,
                threePersons.getExpected());
    }

    @Test
    public void executeDeleteAccountDeleteSelf() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Account myAccount = new Account("myself", "password", "admin");
        threePersons.setBothPersons(1, myAccount);

        final Person self = threePersons.getActualPerson(1);
        privilege.setMyPerson(self);

        assertCommandBehavior("delacc 1",
                DeleteAccountCommand.MESSAGE_DELETING_SELF,
                expected,
                false,
                threePersons.getExpected());
    }

    @Test
    public void executeListAccountShowsEmpty() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        List<? extends ReadOnlyPerson> expectedList = new ArrayList<>();

        assertCommandBehavior("listacc",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeListAccountShowsList() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Account myAccount = new Account("myself", "password", "admin");
        threePersons.setBothPersons(1, myAccount);

        final Account myAccount2 = new Account("myself2", "password2", "admin");
        threePersons.setBothPersons(2, myAccount2);

        List<ReadOnlyPerson> expectedList = new ArrayList<>();
        expectedList.add(threePersons.getExpectedPerson(1));
        expectedList.add(threePersons.getExpectedPerson(2));

        assertCommandBehavior("listacc",
                Command.getMessageForPersonListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeLoginInvalidArgument() throws Exception {
        final TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        p1.setAccount(new Account("username", "password", "Tutor"));
        addressBook.addPerson(p1);

        final AddressBook expectedAddressbook = new AddressBook();
        Person expectedP1 = helper.generatePerson(1, false);
        expectedP1.setAccount(new Account("username", "password", "Tutor"));
        expectedAddressbook.addPerson(expectedP1);
        final PrivilegeLevel initialPrivilege = privilege.getUser().getPrivilegeLevel();

        assertCommandBehavior("login",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, LoginCommand.MESSAGE_USAGE),
                expectedAddressbook,
                false,
                Collections.emptyList());
        final PrivilegeLevel finalPrivilege = privilege.getUser().getPrivilegeLevel();
        assertEquals(initialPrivilege, finalPrivilege);
    }

    @Test
    public void executeLoginInvalidArgumentNumber() throws Exception {
        final TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        p1.setAccount(new Account("username", "password", "Tutor"));
        addressBook.addPerson(p1);

        final AddressBook expectedAddressbook = new AddressBook();
        Person expectedP1 = helper.generatePerson(1, false);
        expectedP1.setAccount(new Account("username", "password", "Tutor"));
        expectedAddressbook.addPerson(expectedP1);

        final String expectedMessage = MESSAGE_WRONG_NUMBER_ARGUMENTS;
        final PrivilegeLevel initialPrivilege = privilege.getUser().getPrivilegeLevel();
        //TODO: Make REQUIRED_ARGUMENT public and use it here instead
        final int requiredArguments = 2;
        int actualArguments = 1;

        assertCommandBehavior("login username",
                String.format(expectedMessage, requiredArguments, actualArguments, LoginCommand.MESSAGE_USAGE),
                expectedAddressbook,
                false,
                Collections.emptyList());

        actualArguments = 1;
        assertCommandBehavior("login password",
                String.format(expectedMessage, requiredArguments, actualArguments, LoginCommand.MESSAGE_USAGE),
                expectedAddressbook,
                false,
                Collections.emptyList());

        actualArguments = 3;
        assertCommandBehavior("login username password extra_argument",
                String.format(expectedMessage, requiredArguments, actualArguments, LoginCommand.MESSAGE_USAGE),
                expectedAddressbook,
                false,
                Collections.emptyList());
        final PrivilegeLevel finalPrivilege = privilege.getUser().getPrivilegeLevel();
        assertEquals(initialPrivilege, finalPrivilege);
    }

    @Test
    public void executeLoginMissingUser() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        threePersons.setBothPersons(1, new Account("username", "password", "Tutor"));

        final PrivilegeLevel initialPrivilege = privilege.getUser().getPrivilegeLevel();

        assertCommandBehavior("login otherUsername password",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                threePersons.getExpected());
        assertCommandBehavior("login password password",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                threePersons.getExpected());
        final PrivilegeLevel finalPrivilege = privilege.getUser().getPrivilegeLevel();
        assertEquals(initialPrivilege, finalPrivilege);
    }

    @Test
    public void executeLoginWrongPassword() throws Exception {
        final TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        p1.setAccount(new Account("username", "password", "Tutor"));
        addressBook.addPerson(p1);

        final AddressBook expectedAddressbook = new AddressBook();
        Person expectedP1 = helper.generatePerson(1, false);
        expectedP1.setAccount(new Account("username", "password", "Tutor"));
        expectedAddressbook.addPerson(expectedP1);
        final PrivilegeLevel initialPrivilege = privilege.getUser().getPrivilegeLevel();

        assertCommandBehavior("login username WrongPassword",
                LoginCommand.MESSAGE_WRONG_PASSWORD,
                expectedAddressbook,
                false,
                Collections.emptyList());
        assertCommandBehavior("login username username",
                LoginCommand.MESSAGE_WRONG_PASSWORD,
                expectedAddressbook,
                false,
                Collections.emptyList());
        assertCommandBehavior("login username password12345",
                LoginCommand.MESSAGE_WRONG_PASSWORD,
                expectedAddressbook,
                false,
                Collections.emptyList());
        final PrivilegeLevel finalPrivilege = privilege.getUser().getPrivilegeLevel();
        assertEquals(initialPrivilege, finalPrivilege);
    }

    @Test
    public void executeLoginSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = new AddressBook();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        threePersons.setBothPersons(1, new Account("tutorUser", "password", "Tutor"));
        threePersons.setBothPersons(2, new Account("B_user", "password2", "Basic"));
        threePersons.setBothPersons(3, new Account("Sudo", "password", "Admin"));

        final Person p1 = threePersons.getActualPerson(1);
        assertCommandBehavior("login tutorUser password",
                String.format(LoginCommand.MESSAGE_SUCCESS, p1.getName(), "Tutor"),
                expected,
                false,
                threePersons.getExpected());
        assertEquals(privilege.getUser(), new TutorUser());

        final Person p2 = threePersons.getActualPerson(2);
        assertCommandBehavior("login B_user password2",
                String.format(LoginCommand.MESSAGE_SUCCESS, p2.getName(), "Basic"),
                expected,
                false,
                threePersons.getExpected());
        assertEquals(privilege.getUser(), new BasicUser());

        final Person p3 = threePersons.getActualPerson(3);
        assertCommandBehavior("login Sudo password",
                String.format(LoginCommand.MESSAGE_SUCCESS, p3.getName(), "Admin"),
                expected,
                false,
                threePersons.getExpected());
        assertEquals(privilege.getUser(), new AdminUser());
    }

    @Test
    public void executeLogoutNotLoggedIn() throws Exception {
        privilege.resetPrivilege();
        assertCommandBehavior("logout", MESSAGE_NOT_LOGGED_IN, TargetType.PERSONS);
    }

    @Test
    public void executeLogoutSuccess() throws Exception {
        assertCommandBehavior("logout", LogoutCommand.MESSAGE_SUCCESS, TargetType.PERSONS);
        assertTrue(privilege.isBase());
    }

    @Test
    public void executeClearStoppedAsLoggedIn() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        TestDataHelper.ThreePersons threePersons = helper.generateThreePersons();
        AddressBook expected = new AddressBook();

        setUpThreePerson(addressBook, expected, logic, threePersons);

        final Person p2 = threePersons.getActualPerson(2);

        privilege.setMyPerson(p2);

        assertCommandBehavior("clear",
                ClearCommand.MESSAGE_DELETING_SELF,
                expected,
                false,
                threePersons.getExpected());
    }
}
