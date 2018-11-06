package seedu.addressbook.logic;

import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.addressbook.common.Messages.MESSAGE_NO_NON_PRIVATE_EXAMS;
import static seedu.addressbook.common.Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK;
import static seedu.addressbook.common.Messages.MESSAGE_WRONG_NUMBER_ARGUMENTS;
import static seedu.addressbook.logic.CommandAssertions.assertCommandBehavior;
import static seedu.addressbook.logic.CommandAssertions.assertInvalidIndexBehaviorForCommand;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.exams.AddExamCommand;
import seedu.addressbook.commands.exams.ClearExamsCommand;
import seedu.addressbook.commands.exams.DeleteExamCommand;
import seedu.addressbook.commands.exams.DeregisterExamCommand;
import seedu.addressbook.commands.exams.EditExamCommand;
import seedu.addressbook.commands.exams.RegisterExamCommand;
import seedu.addressbook.commands.exams.ViewExamsCommand;
import seedu.addressbook.commands.person.ClearCommand;
import seedu.addressbook.commands.person.DeleteCommand;
import seedu.addressbook.commands.person.ViewCommand;
import seedu.addressbook.common.Messages;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.person.Exam;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyExam;
import seedu.addressbook.privilege.Privilege;
import seedu.addressbook.privilege.user.AdminUser;
import seedu.addressbook.storage.StorageFile;
import seedu.addressbook.stubs.StorageStub;

/**
 * For testing of Exams-related Commands
 */
public class ExamCommandsTest {

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private AddressBook addressBook;
    private ExamBook examBook;
    private Logic logic;

    @Before
    public void setUp() throws Exception {
        StorageStub stubFile;
        StorageFile saveFile;
        saveFile = new StorageFile(saveFolder.newFile("testSaveFile.txt").getPath(),
                saveFolder.newFile("testExamFile.txt").getPath(),
                saveFolder.newFile("testStatisticsFile.txt").getPath());
        stubFile = new StorageStub(saveFolder.newFile("testStubFile.txt").getPath(),
                saveFolder.newFile("testStubExamFile.txt").getPath(),
                saveFolder.newFile("testStubStatisticsFile.txt").getPath());

        addressBook = new AddressBook();
        examBook = new ExamBook();
        StatisticsBook statisticBook = new StatisticsBook();
        // Privilege set to admin to allow all commands.
        // Privilege restrictions are tested separately under PrivilegeTest.
        Privilege privilege = new Privilege(new AdminUser());

        saveFile.save(addressBook);
        saveFile.saveExam(examBook);
        logic = new Logic(stubFile, addressBook, examBook, statisticBook, privilege);
        CommandAssertions.setData(saveFile, addressBook, logic, examBook, statisticBook);
    }

    @Test
    public void executeAddExam_validData_success() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Exam toBeAdded = helper.math();
        ExamBook expected = new ExamBook();
        expected.addExam(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddExamCommand(toBeAdded),
                String.format(AddExamCommand.MESSAGE_SUCCESS, toBeAdded),
                expected,
                true,
                expected.getAllExam().immutableListView(),
                true);
    }

    @Test
    public void executeAddExam_duplicateData_duplicateMessage() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Exam toBeAdded = helper.math();
        ExamBook expected = new ExamBook();
        expected.addExam(toBeAdded);

        // setup starting state
        examBook.addExam(toBeAdded); // exam already in internal exam book

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddExamCommand(toBeAdded), AddExamCommand.MESSAGE_DUPLICATE_EXAM,
                expected,
                false,
                Collections.emptyList(),
                false);
    }

    @Test
    public void executeAddExam_invalidArgs_invalidMessage() throws Exception {
        final String invalidDateArg = "d/32122018";
        final String invalidDateArg2 = "d/not_a_number";
        final String validDateArg = "d/" + Exam.EXAM_DATE_EXAMPLE;
        final String invalidStartTimeArg = "st/not__numbers";
        final String invalidStartTimeArg2 = "st/999999";
        final String validStartTimeArg = "st/" + Exam.EXAM_START_TIME_EXAMPLE;
        final String invalidEndTimeArg = "et/not__numbers";
        final String invalidEndTimeArg2 = "et/999999";
        final String validEndTimeArg = "et/" + Exam.EXAM_END_TIME_EXAMPLE;
        final String invalidEndTimeIntervalArg = "et/08:00";

        // exam name, subject name and details can be any string, so no invalid strings
        final String addExamCommandFormatString = "addexam e/" + Exam.EXAM_NAME_EXAMPLE
                + " s/" + Exam.SUBJECT_NAME_EXAMPLE
                + " %s %s %s dt/" + Exam.EXAM_DETAILS_EXAMPLE;

        TestDataHelper helper = new TestDataHelper();
        Exam toBeAdded = helper.math();
        ExamBook expected = new ExamBook();
        expected.addExam(toBeAdded);

        String expectedMessage = Messages.MESSAGE_DATE_CONSTRAINTS;
        assertCommandBehavior(String.format(addExamCommandFormatString, invalidDateArg,
                validStartTimeArg, validEndTimeArg), expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior(String.format(addExamCommandFormatString, invalidDateArg2,
                validStartTimeArg, validEndTimeArg), expectedMessage, CommandAssertions.TargetType.EB);

        expectedMessage = Exam.MESSAGE_TIME_CONSTRAINTS;
        assertCommandBehavior(String.format(addExamCommandFormatString, validDateArg,
                invalidStartTimeArg, validEndTimeArg), expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior(String.format(addExamCommandFormatString, validDateArg,
                invalidStartTimeArg2, validEndTimeArg), expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior(String.format(addExamCommandFormatString, validDateArg,
                validStartTimeArg, invalidEndTimeArg), expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior(String.format(addExamCommandFormatString, validDateArg,
                validStartTimeArg, invalidEndTimeArg2), expectedMessage, CommandAssertions.TargetType.EB);

        expectedMessage = Exam.MESSAGE_TIME_INTERVAL_CONSTRAINTS;
        assertCommandBehavior(String.format(addExamCommandFormatString, validDateArg,
                validStartTimeArg, invalidEndTimeIntervalArg), expectedMessage, CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeExamsListShowsAllExams() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        ExamBook expected = helper.generateExamBook(false, true);
        List<? extends ReadOnlyExam> expectedList = expected.getAllExam().immutableListView();

        // prepare exam book state
        helper.addToExamBook(examBook, false, true);

        assertCommandBehavior("examslist",
                Command.getMessageForExamListShownSummary(expectedList),
                expected,
                true,
                expectedList);
    }

    @Test
    public void executeClearExamsSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        // generates the 3 test exam and execute the add exam command
        for (int i = 1; i <= 3; ++i) {
            final Exam testExam = helper.generateExam(i, true);
            examBook.addExam(testExam);
            logic.execute(helper.generateAddExamCommand(testExam));
        }
        assertCommandBehavior("clearexams",
                ClearExamsCommand.MESSAGE_SUCCESS,
                ExamBook.empty(),
                true,
                Collections.emptyList(),
                true);
    }

    @Test
    public void executeDeleteExamRemovesCorrectExam_validData_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsNoTakers(helper);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        ExamBook expected = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true);
        expected.removeExam(e2);

        assertCommandBehavior("deleteexam 2",
                String.format(DeleteExamCommand.MESSAGE_DELETE_EXAM_SUCCESS, e2),
                expected,
                true,
                expected.getAllExam().immutableListView(),
                true);
    }

    @Test
    public void executeDeleteExam_invalidArgsFormat_invalidCommandMessage() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("deleteexam ", expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior("deleteexam arg not number", expectedMessage, CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeDeleteExam_invalidIndex_invalidIndexMessage() throws Exception {
        assertInvalidIndexBehaviorForCommand("deleteexam", CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeDeleteExam_missingInExamBook_examMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsNoTakers(helper);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        ExamBook expected = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true);
        expected.removeExam(e2);
        examBook.removeExam(e2);

        assertCommandBehavior("deleteexam 2",
                Messages.MESSAGE_EXAM_NOT_IN_EXAMBOOK,
                expected,
                false,
                threeExams);
    }

    @Test
    public void executeEditExam_validData_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsNoTakers(helper);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        ExamBook expected = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true);
        Exam e4 = helper.generateExam(4, false);
        expected.editExam(e2, e4);

        assertCommandBehavior("editexam 2 p/n e/Exam 4 s/Subject 4 d/01-02-2018 dt/Held in 4",
                String.format(EditExamCommand.MESSAGE_EDIT_EXAM_SUCCESS, e2, e4),
                expected,
                true,
                expected.getAllExam().immutableListView(),
                true);
    }

    @Test
    public void executeEditExam_invalidArgsFormat_invalidCommandMessage() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("editexam ", expectedMessage, CommandAssertions.TargetType.EB);
        expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("editexam arg not number", expectedMessage, CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeEditExam_invalidIndex_invalidIndexMessage() throws Exception {
        assertInvalidIndexBehaviorForCommand("editexam 4 s/Mathematics", CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeEditExam_invalidArgs_invalidMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsNoTakers(helper);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        ExamBook expected = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true);
        Exam e4 = helper.generateExam(4, false);
        expected.editExam(e2, e4);

        String expectedMessage = Messages.MESSAGE_DATE_CONSTRAINTS;
        assertCommandBehavior("editexam 1 d/notADate", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 d/07012018 st/09:00", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 d/32122018 st/09:00", expectedMessage,
                examBook, logic.getLastShownExamList());

        expectedMessage = Exam.MESSAGE_TIME_CONSTRAINTS;
        assertCommandBehavior("editexam 1 st/notATime", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 st/07012018", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 st/2366", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 et/notATime", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 et/07012018", expectedMessage,
                examBook, logic.getLastShownExamList());
        assertCommandBehavior("editexam 1 et/2366", expectedMessage,
                examBook, logic.getLastShownExamList());

        expectedMessage = Exam.MESSAGE_TIME_INTERVAL_CONSTRAINTS;
        assertCommandBehavior("editexam 1 st/09:00 et/08:00", expectedMessage,
                examBook, logic.getLastShownExamList());

        expectedMessage = EditExamCommand.MESSAGE_DUPLICATE_EXAM;
        Exam e1 = helper.generateExam(1, true);
        assertCommandBehavior(String.format("editexam 1 e/%s", e1.getExamName()), expectedMessage,
                examBook, logic.getLastShownExamList());

        expectedMessage = EditExamCommand.MESSAGE_PRIVATE_CONSTRAINTS;
        assertCommandBehavior("editexam 1 p/ok", expectedMessage,
                examBook, logic.getLastShownExamList());
    }

    @Test
    public void executeEdit_missingInExamBook_examMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsNoTakers(helper);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        ExamBook expected = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true);
        expected.removeExam(e2);

        examBook.removeExam(e2);

        assertCommandBehavior("editexam 2 s/Mathematics",
                Messages.MESSAGE_EXAM_NOT_IN_EXAMBOOK,
                expected,
                false,
                threeExams);
    }

    @Test
    public void executeRegisterExam_validArgsSingleTaker_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        Exam e1Expected = helper.generateExam(1, false, 1);
        List<Exam> singleExamExpected = helper.generateExamList(e1Expected);
        ExamBook expectedExamBook = helper.generateExamBook(singleExamExpected);

        Person p1Expected = helper.generatePerson(1, false, 1, false , 1);
        List<Person> personListExpected = helper.generatePersonList(p1Expected);
        AddressBook expectedBook = helper.generateAddressBook(personListExpected);

        assertCommandBehavior("regexam 1 1",
                String.format(RegisterExamCommand.MESSAGE_REGISTER_EXAM_SUCCESS, p1Expected),
                expectedBook,
                expectedExamBook,
                false,
                false,
                logic.getLastShownList(),
                logic.getLastShownExamList());
    }

    @Test
    public void executeRegisterExam_validArgsMultiTakers_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsWithTakers(helper, 2, 0, 0);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        Person p1 = helper.generatePerson(1, false, 1, false, 2);
        Person p2 = helper.generatePerson(2, false, 1, false, 2);
        Person p3 = helper.generatePerson(3, false);
        List<Person> personList = helper.generatePersonList(p1, p2, p3);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        List<Exam> threeExamsExpected = setUpThreeExamsWithTakers(helper, 3, 0, 0);
        ExamBook expectedExamBook = helper.generateExamBook(threeExamsExpected);

        Person p1Expected = helper.generatePerson(1, false, 1 , false, 3);
        Person p2Expected = helper.generatePerson(2, false, 1 , false, 3);
        Person p3Expected = helper.generatePerson(3, false, 1, false, 3);
        List<Person> personListExpected = helper.generatePersonList(p1Expected, p2Expected, p3Expected);
        AddressBook expectedBook = helper.generateAddressBook(personListExpected);

        assertCommandBehavior("regexam 3 1",
                String.format(RegisterExamCommand.MESSAGE_REGISTER_EXAM_SUCCESS, p3Expected),
                expectedBook,
                expectedExamBook,
                false,
                false,
                logic.getLastShownList(),
                logic.getLastShownExamList());
    }

    @Test
    public void executeRegisterExam_invalidParsedArgs() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RegisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("regexam not_a_number 2", expectedMessage,
                CommandAssertions.TargetType.EB);
        assertCommandBehavior("regexam 2 not_a_number", expectedMessage,
                CommandAssertions.TargetType.EB);
        assertCommandBehavior("regexam not_a_number not_a_number", expectedMessage,
                CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeRegisterExam_validArgsSingleTakerAlreadyRegistered_registerDuplicateMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsWithTakers(helper, 1, 0, 0);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        Person p1 = helper.generatePerson(1, false, 1 , false, 1);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("regexam 1 1",
                RegisterExamCommand.MESSAGE_EXAM_ALREADY_REGISTERED,
                addressBook,
                examBook,
                false,
                false,
                logic.getLastShownList(),
                logic.getLastShownExamList());
    }

    @Test
    public void executeRegisterExam_invalidNumberOfArgs_invalidNumberMessage() throws Exception {
        String expectedMessage = String.format(MESSAGE_WRONG_NUMBER_ARGUMENTS, 2, 1,
                RegisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("regexam 1", expectedMessage, CommandAssertions.TargetType.EB);

        expectedMessage = String.format(MESSAGE_WRONG_NUMBER_ARGUMENTS, 2, 3,
                RegisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("regexam 1 1 1", expectedMessage, CommandAssertions.TargetType.EB);

        expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RegisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("regexam", expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior("regexam ", expectedMessage, CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeRegisterExam_invalidArgsForPersonIndex_invalidPersonIndexMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("regexam 2 1",
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());

        assertCommandBehavior("regexam 2 2",
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeRegisterExam_invalidArgsForExamIndex_invalidExamIndexMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("regexam 1 2",
                Messages.MESSAGE_INVALID_EXAM_DISPLAYED_INDEX,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeRegisterExam_personMissingInAddressBook_personMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        logic.setLastShownList(personList);

        assertCommandBehavior("regexam 1 1",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeRegisterExam_examMissingInExamBook_examMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false, 1, false, 0);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("regexam 1 1",
                Messages.MESSAGE_EXAM_NOT_IN_EXAMBOOK,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_validArgsSingleTaker_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false, 1);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false, 1 , false, 1);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        Exam e1Expected = helper.generateExam(1, false);
        List<Exam> singleExamExpected = helper.generateExamList(e1Expected);
        ExamBook expectedExamBook = helper.generateExamBook(singleExamExpected);

        Person p1Expected = helper.generatePerson(1, false);
        List<Person> personListExpected = helper.generatePersonList(p1Expected);
        AddressBook expectedBook = helper.generateAddressBook(personListExpected);

        assertCommandBehavior("deregexam 1 1",
                String.format(DeregisterExamCommand.MESSAGE_DEREGISTER_EXAM_SUCCESS, e1Expected, p1Expected),
                expectedBook,
                expectedExamBook,
                false,
                false,
                logic.getLastShownList(),
                logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_validArgsMultiTakers_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> singleExam = setUpThreeExamsWithTakers(helper, 0, 2 , 0);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        List<Person> personList = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        Exam e2Expected = helper.generateExam(2, true, 1);
        List<Exam> singleExamExpected = setUpThreeExamsWithTakers(helper, 0, 1, 0);
        ExamBook expectedExamBook = helper.generateExamBook(singleExamExpected);

        Person p1 = helper.generatePerson(1, true, 2 , true, 1);
        Person p2 = helper.generatePerson(2, true);
        Person p3 = helper.generatePerson(3, true, 3, false, 1);
        List<Person> personListExpected = helper.generatePersonList(p1, p2, p3);
        AddressBook expectedBook = helper.generateAddressBook(personListExpected);

        assertCommandBehavior("deregexam 2 2",
                String.format(DeregisterExamCommand.MESSAGE_DEREGISTER_EXAM_SUCCESS, e2Expected, p2),
                expectedBook, expectedExamBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_validArgsSingleTakerNotRegistered_notRegisteredMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("deregexam 1 1",
                DeregisterExamCommand.MESSAGE_EXAM_NOT_REGISTERED,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_invalidNumberOfArgs() throws Exception {
        String expectedMessage = String.format(MESSAGE_WRONG_NUMBER_ARGUMENTS , 2, 1,
                DeregisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("deregexam 1", expectedMessage, CommandAssertions.TargetType.EB);

        expectedMessage = String.format(MESSAGE_WRONG_NUMBER_ARGUMENTS , 2, 3,
                DeregisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("deregexam 1 1 1", expectedMessage, CommandAssertions.TargetType.EB);

        expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeregisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("deregexam", expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior("deregexam ", expectedMessage, CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeDeregisterExam_invalidParsedArgs_invalidCommandMessage() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeregisterExamCommand.MESSAGE_USAGE);
        assertCommandBehavior("deregexam not_a_number 2", expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior("deregexam 2 not_a_number", expectedMessage, CommandAssertions.TargetType.EB);
        assertCommandBehavior("deregexam not_a_number not_a_number", expectedMessage, CommandAssertions.TargetType.EB);
    }

    @Test
    public void executeDeregisterExam_invalidArgsForPersonIndex_invalidPersonIndexMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("deregexam 2 1",
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
        assertCommandBehavior("deregexam 2 2",
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_invalidArgsForExamIndex_invalidExamIndexMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("deregexam 1 2",
                Messages.MESSAGE_INVALID_EXAM_DISPLAYED_INDEX,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_personMissingInAddressBook_personMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        List<Exam> singleExam = helper.generateExamList(e1);
        helper.addToExamBook(examBook, singleExam);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        logic.setLastShownList(personList);

        assertCommandBehavior("deregexam 1 1",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeDeregisterExam_examMissingInExamBook_examMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false, 1);
        List<Exam> singleExam = helper.generateExamList(e1);
        logic.setLastShownExamList(singleExam);

        Person p1 = helper.generatePerson(1, false);
        List<Person> personList = helper.generatePersonList(p1);
        helper.addToAddressBook(addressBook, personList);
        logic.setLastShownList(personList);

        assertCommandBehavior("deregexam 1 1",
                Messages.MESSAGE_EXAM_NOT_IN_EXAMBOOK,
                addressBook, examBook, false, false,
                logic.getLastShownList(), logic.getLastShownExamList());
    }

    @Test
    public void executeViewExam_invalidArgsFormat_invalidCommandMessage() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewExamsCommand.MESSAGE_USAGE);
        assertCommandBehavior("viewexams ", expectedMessage, CommandAssertions.TargetType.AB);
        assertCommandBehavior("viewexams arg not number", expectedMessage, CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeViewExams_invalidIndex_invalidIndexMessage() throws Exception {
        assertInvalidIndexBehaviorForCommand("viewexams", CommandAssertions.TargetType.AB);
    }

    @Test
    public void executeViewExams_validArgs_hidesPrivateExams() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e2 = helper.generateExam(2, true, 1);

        Person p1 = helper.generatePerson(1, false, 1, false , 2);
        Person p2 = helper.generatePerson(2, false, 1, false, 2);
        Person p2Viewable = helper.generatePerson(2, false, 1, false, 2);
        Person p3 = helper.generatePerson(3, false);
        p2.addExam(e2);
        List<Person> lastShownList = helper.generatePersonList(p1, p2, p3);
        helper.addToAddressBook(addressBook, lastShownList);
        logic.setLastShownList(lastShownList);

        AddressBook expected = helper.generateAddressBook(lastShownList);

        assertCommandBehavior("viewexams 1",
                String.format(ViewExamsCommand.MESSAGE_VIEW_EXAMS_PERSON_SUCCESS, p1.getAsTextShowOnlyName()),
                p1.getAsTextShowExam(),
                expected,
                false,
                lastShownList);

        assertCommandBehavior("viewexams 2",
                String.format(ViewExamsCommand.MESSAGE_VIEW_EXAMS_PERSON_SUCCESS, p2.getAsTextShowOnlyName()),
                p2.getAsTextShowExam(),
                expected,
                false,
                lastShownList);

        assertCommandBehavior("viewexams 2",
                String.format(ViewExamsCommand.MESSAGE_VIEW_EXAMS_PERSON_SUCCESS, p2Viewable.getAsTextShowOnlyName()),
                p2Viewable.getAsTextShowExam(),
                expected,
                false,
                lastShownList);

        assertCommandBehavior("viewexams 3",
                String.format(ViewExamsCommand.MESSAGE_VIEW_EXAMS_PERSON_SUCCESS, p3.getAsTextShowOnlyName()),
                MESSAGE_NO_NON_PRIVATE_EXAMS,
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeTryToViewExams_missingPerson_personMissingMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, false);
        Person p2 = helper.generatePerson(2, false, 2, false, 1);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);
        logic.setLastShownList(lastShownList);
        addressBook.addPerson(p1);

        AddressBook expected = new AddressBook();
        expected.addPerson(p1);

        assertCommandBehavior("viewexams 2",
                MESSAGE_PERSON_NOT_IN_ADDRESSBOOK,
                expected,
                false,
                lastShownList);
    }

    @Test
    public void executeClear_validData_success() throws Exception {
        TestDataHelper helper = new TestDataHelper();

        List<Person> threePersons = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, threePersons);

        List<Exam> threeExams = setUpThreeExamsWithTakers(helper, 0, 2, 1);
        helper.addToExamBook(examBook, threeExams);

        List<Exam> examExpected = setUpThreeExamsNoTakers(helper);
        ExamBook expectedExamBook = helper.generateExamBook(examExpected);

        assertCommandBehavior("clear",
                ClearCommand.MESSAGE_SUCCESS,
                AddressBook.empty(),
                expectedExamBook,
                true,
                false,
                Collections.emptyList(),
                Collections.emptyList());
    }

    @Test
    public void executeDelete_validData_removesCorrectPersonAndExam() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p2 = helper.generatePerson(2, true, 2, true, 2);
        Person p3 = helper.generatePerson(3, true, 3, false, 1);
        List<Person> lastShownList = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, lastShownList);
        logic.setLastShownList(lastShownList);

        Exam e2 = helper.generateExam(2, true, 2);
        Exam e3 = helper.generateExam(3, false, 1);
        examBook.addExam(e2);
        examBook.addExam(e3);

        Exam e2Expected = helper.generateExam(2, true, 1);
        List<Exam> expectedExamList = helper.generateExamList(e2Expected);
        ExamBook expectedExamBook = helper.generateExamBook(expectedExamList);
        expectedExamBook.addExam(e3);

        Person p1 = helper.generatePerson(1, true, 2, true, 1);
        List<Person> newList = helper.generatePersonList(p1, p3);
        AddressBook expectedAddressBook = helper.generateAddressBook(newList);

        assertCommandBehavior("delete 2",
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, p2),
                expectedAddressBook,
                expectedExamBook,
                true,
                false,
                newList,
                Collections.emptyList());
    }

    @Test
    public void executeDelete_validArgs_removesCorrectPersonMissingExamMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Person> lastShownList = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, lastShownList);
        logic.setLastShownList(lastShownList);

        Exam e3 = helper.generateExam(3, false, 1);
        examBook.addExam(e3);

        assertCommandBehavior("delete 2",
                Messages.MESSAGE_EXAM_NOT_IN_EXAMBOOK, examBook, false,
                Collections.emptyList(), false);
    }

    @Test
    public void executeClearExams_validFormat_successWithAddressBook() throws Exception {
        TestDataHelper helper = new TestDataHelper();

        List<Exam> threeExams = setUpThreeExamsWithTakers(helper , 0, 2, 1);
        helper.addToExamBook(examBook, threeExams);

        List<Person> threePersons = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, threePersons);

        Person newP1 = helper.generatePerson(1, true);
        Person newP2 = helper.generatePerson(2, true);
        Person newP3 = helper.generatePerson(3, true);
        List<Person> newList = helper.generatePersonList(newP1, newP2, newP3);
        AddressBook expectedAddressBook = helper.generateAddressBook(newList);

        assertCommandBehavior("clearexams",
                ClearExamsCommand.MESSAGE_SUCCESS,
                expectedAddressBook,
                ExamBook.empty(),
                false,
                true,
                Collections.emptyList(),
                Collections.emptyList());
    }

    @Test
    public void executeDeleteExam_validArgs_removesCorrectExamInAddressBook() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsWithTakers(helper, 0, 2, 1);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        List<Person> threePersons = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, threePersons);

        ExamBook expectedExamBook = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true, 2);
        expectedExamBook.removeExam(e2);

        Person newP1 = helper.generatePerson(1, true);
        Person newP2 = helper.generatePerson(2, true);
        Person newP3 = helper.generatePerson(3, true, 3, false, 1);
        List<Person> newList = helper.generatePersonList(newP1, newP2, newP3);
        AddressBook expectedAddressBook = helper.generateAddressBook(newList);

        assertCommandBehavior("deleteexam 2",
                String.format(DeleteExamCommand.MESSAGE_DELETE_EXAM_SUCCESS, e2),
                expectedAddressBook,
                expectedExamBook,
                false,
                true,
                Collections.emptyList(),
                expectedExamBook.getAllExam().immutableListView());
    }

    @Test
    public void executeEditExam_validArgs_changesAddressBookSuccess() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Exam> threeExams = setUpThreeExamsWithTakers(helper, 0, 2, 1);
        helper.addToExamBook(examBook, threeExams);
        logic.setLastShownExamList(threeExams);

        ExamBook expectedExamBook = helper.generateExamBook(threeExams);
        Exam e2 = helper.generateExam(2, true, 2);
        Exam e4 = helper.generateExam(4, false, 2);
        expectedExamBook.editExam(e2, e4);

        List<Person> threePersons = setUpThreePersonsStandardExam(helper);
        helper.addToAddressBook(addressBook, threePersons);

        Person newP1 = helper.generatePerson(1, true, 4, false, 2);
        Person newP2 = helper.generatePerson(2, true, 4, false, 2);
        Person newP3 = helper.generatePerson(3, true, 3, false, 1);
        List<Person> newList = helper.generatePersonList(newP1, newP2, newP3);
        AddressBook expectedAddressBook = helper.generateAddressBook(newList);

        assertCommandBehavior("editexam 2 p/n e/Exam 4 s/Subject 4 d/01-02-2018 dt/Held in 4",
                String.format(EditExamCommand.MESSAGE_EDIT_EXAM_SUCCESS, e2, e4),
                expectedAddressBook,
                expectedExamBook,
                false,
                true,
                Collections.emptyList(),
                expectedExamBook.getAllExam().immutableListView());
    }

    @Test
    public void executeViewAllAlsoShowsPrivateAndAllExams() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePerson(1, true, 1 , true, 1);
        Person p2 = helper.generatePerson(2, false, 2, false , 1);
        List<Person> lastShownList = helper.generatePersonList(p1, p2);
        helper.addToAddressBook(addressBook, lastShownList);
        logic.setLastShownList(lastShownList);

        AddressBook expected = helper.generateAddressBook(lastShownList);

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

    private List<Exam> setUpThreeExamsNoTakers(TestDataHelper helper) throws Exception {
        Exam e1 = helper.generateExam(1, false);
        Exam e2 = helper.generateExam(2, true);
        Exam e3 = helper.generateExam(3, false);
        List<Exam> threeExams = helper.generateExamList(e1, e2, e3);
        return threeExams;
    }

    private List<Exam> setUpThreeExamsWithTakers(TestDataHelper helper, int taker1,
                                                 int taker2, int taker3) throws Exception {
        Exam e1 = helper.generateExam(1, false, taker1);
        Exam e2 = helper.generateExam(2, true, taker2);
        Exam e3 = helper.generateExam(3, false, taker3);
        List<Exam> threeExams = helper.generateExamList(e1, e2, e3);
        return threeExams;
    }

    private List<Person> setUpThreePersonsStandardExam(TestDataHelper helper) throws Exception {
        Person p1 = helper.generatePerson(1, true, 2, true, 2);
        Person p2 = helper.generatePerson(2, true, 2, true, 2);
        Person p3 = helper.generatePerson(3, true, 3, false, 1);
        List<Person> threePersons = helper.generatePersonList(p1, p2, p3);
        return threePersons;
    }
}
