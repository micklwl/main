package seedu.addressbook.logic;

import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.addressbook.common.Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK;
import static seedu.addressbook.logic.CommandAssertions.TargetType;
import static seedu.addressbook.logic.CommandAssertions.assertCommandBehavior;
import static seedu.addressbook.logic.CommandAssertions.assertInvalidIndexBehaviorForCommand;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.fees.EditFeesCommand;
import seedu.addressbook.commands.fees.ViewFeesCommand;
import seedu.addressbook.commands.person.ViewCommand;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.person.Fees;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.privilege.Privilege;
import seedu.addressbook.privilege.user.AdminUser;
import seedu.addressbook.stubs.StorageStub;

/**
 * For testing of Fees-related Commands
 */
public class FeesCommandsTest {

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();
    private Logic logic;
    private AddressBook addressBook;

    @Before
    public void setUp() throws Exception {
        addressBook = new AddressBook();
        ExamBook examBook = new ExamBook();
        StatisticsBook statisticBook = new StatisticsBook();
        // Privilege set to admin to allow all commands.
        // Privilege restrictions are tested separately under PrivilegeTest.
        Privilege privilege = new Privilege(new AdminUser());


        StorageStub stubFile = new StorageStub(saveFolder.newFile("testStubFile.txt").getPath(),
                saveFolder.newFile("testStubExamFile.txt").getPath(),
                saveFolder.newFile("testStubStatisticsFile.txt").getPath());
;
        logic = new Logic(stubFile, addressBook, examBook, statisticBook, privilege);
        CommandAssertions.setData(stubFile, addressBook, logic);
    }

    @Test
    public void executeAddFeesCommandInvalidData() throws Exception {
        assertCommandBehavior(
                "editfees 2 1.111 01-01-2018", Fees.MESSAGE_FEES_CONSTRAINTS, TargetType.PERSONS);
    }

    @Test
    public void executeAddFeesSuccessful() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, true);
        Person p3 = helper.generatePerson(3, true);

        List<Person> threePersons = helper.generatePersonList(p1, p2, p3);

        AddressBook expected = helper.generateAddressBook(threePersons);
        expected.findPerson(p2).setFees(helper.fees());

        helper.addToAddressBook(addressBook, threePersons);
        logic.setLastShownList(threePersons);
        assertCommandBehavior(helper.generateEditFeesCommand(),
                String.format(EditFeesCommand.MESSAGE_SUCCESS, p2.getAsTextShowFee()),
                expected,
                false,
                threePersons);
    }

    @Test
    public void executeAddFeesUpdateZero() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p2 = helper.generatePerson(2, true);
        Tag temp = new Tag("feesdue");
        p2.getTags().add(temp);

        List<Person> threePersons = helper.generatePersonList(p2);

        AddressBook expected = helper.generateAddressBook(threePersons);

        helper.addToAddressBook(addressBook, threePersons);
        logic.setLastShownList(threePersons);

        assertCommandBehavior("editfees 1 0.00 00-00-0000",
                String.format(EditFeesCommand.MESSAGE_SUCCESS, p2.getAsTextShowFee()),
                expected,
                false,
                threePersons);

        assertCommandBehavior("viewall 1",
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_DETAILS, p2.getName()),
                p2.getAsTextShowAll(),
                expected,
                false,
                threePersons);
    }

    @Test
    public void executeAddFeesInvalidPerson() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, false);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);

        AddressBook expected = new AddressBook();
        expected.addPerson(p1);

        addressBook.addPerson(p1);
        logic.setLastShownList(lastShownList);

        assertCommandBehavior("editfees 2 0.00 00-00-0000",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeListFeesSuccessful() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = helper.generateAddressBook(false, true);
        List<? extends ReadOnlyPerson> expectedList = expected.listFeesPerson();

        // prepare address book state
        helper.addToAddressBook(addressBook, false, true);

        assertCommandBehavior("listfees",
                Command.getMessageForFeesListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeListFeesEmpty() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        List<? extends ReadOnlyPerson> expectedList = new ArrayList<>();
        List<Person> list = new ArrayList<>();
        AddressBook expected = helper.generateAddressBook(list);

        assertCommandBehavior("listfees",
                Command.getMessageForFeesListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeListDueFeesSuccessful() throws Exception {
        // prepare expectations
        String date = java.time.LocalDate.now().toString();
        TestDataHelper helper = new TestDataHelper();
        AddressBook expected = helper.generateAddressBook(false, true);
        List<? extends ReadOnlyPerson> expectedList = expected.dueFeesPerson(date);

        // prepare address book state
        helper.addToAddressBook(addressBook, false, true);

        assertCommandBehavior("listdue",
                Command.getMessageForFeesListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeListDueFeesWithUpdate() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, true);
        Person p3 = helper.generatePerson(3, true);

        List<Person> threePersons = helper.generatePersonList(p1, p2, p3);

        AddressBook expected = helper.generateAddressBook(threePersons);
        expected.findPerson(p2).setFees(helper.fees());

        List<Person> threePerson = helper.generatePersonList(p1, p2, p3);

        AddressBook temp = helper.generateAddressBook(threePerson);
        List<? extends ReadOnlyPerson> expectedList = temp.dueFeesPerson("0000-00-00");
        List<Person> twoPerson = helper.generatePersonList(p1, p3);
        AddressBook expected2 = helper.generateAddressBook(twoPerson);
        helper.addToAddressBook(addressBook, twoPerson);
        logic.setLastShownList(twoPerson);

        assertCommandBehavior("listdue",
                Command.getMessageForFeesListShownSummary(expectedList),
                expected2,
                true,
                expectedList);
    }

    @Test
    public void executeListDueFeesEmpty() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        List<? extends ReadOnlyPerson> expectedList = new ArrayList<>();
        List<Person> list = new ArrayList<>();
        AddressBook expected = helper.generateAddressBook(list);

        assertCommandBehavior("listdue",
                Command.getMessageForFeesListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeViewFeesCommandSuccessful() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, true);
        Person p3 = helper.generatePerson(3, true);

        List<Person> threePersons = helper.generatePersonList(p1, p2, p3);

        AddressBook expected = helper.generateAddressBook(threePersons);
        expected.findPerson(p2).setFees(helper.fees());

        helper.addToAddressBook(addressBook, threePersons);
        logic.setLastShownList(threePersons);
        assertCommandBehavior("viewfees 2",
                String.format(ViewFeesCommand.MESSAGE_VIEWFEE_PERSON_SUCCESS, p2.getAsTextShowFee()),
                expected,
                false,
                threePersons);
    }

    @Test
    public void executeViewFeesCommandInvalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("viewfees", TargetType.PERSONS);
    }

    @Test
    public void executeViewFeesInvalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewFeesCommand.MESSAGE_USAGE);
        assertCommandBehavior("viewfees ", expectedMessage, TargetType.PERSONS);
        assertCommandBehavior("viewfees arg not number", expectedMessage, TargetType.PERSONS);
    }
}
