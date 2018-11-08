package seedu.addressbook.logic;

import static seedu.addressbook.logic.CommandAssertions.assertCommandBehavior;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.assessment.AddAssessmentCommand;
import seedu.addressbook.commands.assessment.AddAssignmentStatistics;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.person.Assessment;
import seedu.addressbook.data.person.AssignmentStatistics;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.privilege.Privilege;
import seedu.addressbook.privilege.user.AdminUser;
import seedu.addressbook.stubs.StorageStub;

/**
 * For testing of Assessment-related Commands
 */
public class AssessmentCommandsTest {

    /**
     *  See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();
   // private Logic logic;
    private AddressBook addressBook;
    private StatisticsBook statisticBook;
    //private Logic logic; Temporary left as local variable

    @Before
    public void setUp() throws Exception {

        StorageStub stubFile = new StorageStub(saveFolder.newFile("testStubFile.txt").getPath(),
                saveFolder.newFile("testStubExamFile.txt").getPath(),
                saveFolder.newFile("testStubStatisticsFile.txt").getPath());

        addressBook = new AddressBook();
        ExamBook examBook = new ExamBook();
        statisticBook = new StatisticsBook();
        // Privilege set to admin to allow all commands.
        // Privilege restrictions are tested separately under PrivilegeTest.
        Privilege privilege = new Privilege(new AdminUser());

        Logic logic = new Logic(stubFile, addressBook, examBook, statisticBook, privilege);
        CommandAssertions.setData(stubFile, addressBook, logic, examBook, statisticBook);
    }


    @Test
    public void executeAddAssignmentStatisticsSuccessful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        AssignmentStatistics toBeAdded = helper.stat();
        StatisticsBook expected = new StatisticsBook();
        expected.addStatistic(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddAssignmentStatistics(toBeAdded),
                String.format(AddAssignmentStatistics.MESSAGE_SUCCESS, toBeAdded),
                expected, false);
    }

    @Test
    public void executeAddAssignmentStatisticsDuplicateNotAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        AssignmentStatistics toBeAdded = helper.stat();
        StatisticsBook expected = new StatisticsBook();
        expected.addStatistic(toBeAdded);

        // setup starting state
        statisticBook.addStatistic(toBeAdded); // statistic already in internal statistic book

        // execute command and verify result
        assertCommandBehavior(helper.generateAddAssignmentStatistics(toBeAdded),
                AddAssignmentStatistics.MESSAGE_DUPLICATE_STATISTIC, expected, false);
    }

    @Test
    public void executeAddAssessmentSuccessful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Assessment toBeAdded = helper.assess();
        AddressBook expected = new AddressBook();
        expected.addAssessment(toBeAdded);
        List<? extends ReadOnlyPerson> dummyList = expected.getAllPersons().immutableListView();

        // execute command and verify result
        assertCommandBehavior(helper.generateAddAssessment(toBeAdded),
                String.format(AddAssessmentCommand.MESSAGE_SUCCESS, toBeAdded), expected, false,
                dummyList);
    }

    @Test
    public void executeAddAssessmentDuplicateNotAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Assessment toBeAdded = helper.assess();
        AddressBook expected = new AddressBook();
        expected.addAssessment(toBeAdded);
        List<? extends ReadOnlyPerson> dummyList = expected.getAllPersons().immutableListView();

        // setup starting state
        addressBook.addAssessment(toBeAdded); // statistic already in internal statistic book

        // execute command and verify result
        assertCommandBehavior(helper.generateAddAssessment(toBeAdded),
                AddAssessmentCommand.MESSAGE_DUPLICATE_ASSESSMENT, expected, false, dummyList);
    }

}
