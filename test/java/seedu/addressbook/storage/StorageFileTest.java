package seedu.addressbook.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static seedu.addressbook.util.TestUtil.assertTextFilesEqual;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.TestDataHelper;
import seedu.addressbook.common.Pair;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.ExamBook;
import seedu.addressbook.data.StatisticsBook;
import seedu.addressbook.data.account.Account;
import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.person.AssignmentStatistics;
import seedu.addressbook.data.person.Exam;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.details.Address;
import seedu.addressbook.data.person.details.Email;
import seedu.addressbook.data.person.details.Name;
import seedu.addressbook.data.person.details.Phone;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.storage.Storage.StorageOperationException;

public class StorageFileTest {
    private static final String TEST_DATA_FOLDER = "test/data/StorageFileTest";
    private static final String VALID_EXAM_DATA_PATH = "ValidExamData.txt";
    private static final String VALID_STATISTICS_DATA_PATH = "ValidStatisticsData.txt";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void constructor_nullFilePath_exceptionThrown() throws Exception {
        thrown.expect(NullPointerException.class);
        new StorageFile(null, TEST_DATA_FOLDER + "/" + "examBook.txt",
                TEST_DATA_FOLDER + "/" + "statisticsBook.txt");
    }

    @Test
    public void constructor_nullExamFilePath_exceptionThrown() throws Exception {
        thrown.expect(NullPointerException.class);
        new StorageFile(TEST_DATA_FOLDER + "/" + "addressBook.txt", null, null);
    }

    @Test
    public void constructor_nullBothFilePath_exceptionThrown() throws Exception {
        thrown.expect(NullPointerException.class);
        new StorageFile(null, null, null);
    }

    @Test
    public void constructor_defaultPath() throws Exception {
        StorageFile storage = new StorageFile();
        assertNotNull(storage);
    }

    @Test
    public void constructor_noTxtAddressBookExtension_exceptionThrown() throws Exception {
        thrown.expect(IllegalValueException.class);
        new StorageFile(TEST_DATA_FOLDER + "/" + "InvalidfileName", TEST_DATA_FOLDER + "/" + "exams.txt",
                TEST_DATA_FOLDER + "/" + "statistics.txt");
    }

    @Test
    public void constructor_noTxtExamBookExtension_exceptionThrown() throws Exception {
        thrown.expect(IllegalValueException.class);
        new StorageFile(TEST_DATA_FOLDER + "/" + "addressbook.txt", TEST_DATA_FOLDER + "/" + "InvalidExamfileName",
                TEST_DATA_FOLDER + "/" + "InvalidStatisticsfileName.txt");
    }

    @Test
    public void load_invalidAccountFormat_exceptionThrown() throws Exception {
        // The file contains valid xml data, but does not match the AddressBook class
        StorageFile storage = getStorage("InvalidMissingAccountFieldData.txt", "ValidExamData.txt",
                "ValidStatistics.txt");
        thrown.expect(StorageOperationException.class);
        storage.load();
    }

    @Test
    public void load_invalidExamFormatInAddressBook_exceptionThrown() throws Exception {
        // The file contains valid xml data, but does not match the AddressBook class
        StorageFile storage = getStorage("InvalidMissingExamFieldData.txt", "ValidExamData.txt",
                "ValidStatistics.txt");
        thrown.expect(StorageOperationException.class);
        storage.load();
    }

    @Test
    public void load_invalidFieldFormat_exceptionThrown() throws Exception {
        // The file contains valid xml data, but contains an invalid field
        StorageFile storage = getStorage("InvalidFieldData.txt", "ValidExamData.txt",
                "ValidStatistics.txt");
        thrown.expect(StorageOperationException.class);
        final String errorMessage = "Error processing Ke$ha: Person names should be spaces or alphanumeric characters";
        assertReturnsExceptionMessage(storage, errorMessage);
    }

    @Test
    public void load_invalidDuplicateAccountData_exceptionThrown() throws Exception {
        StorageFile storage = getStorage("InvalidDuplicateUsernameData.txt", "ValidExamData.txt",
                "ValidStatistics.txt");
        thrown.expect(StorageOperationException.class);
        final String errorMessage = "Data contains duplicate username";
        assertReturnsExceptionMessage(storage, errorMessage);
    }

    @Test
    public void load_invalidFormat_exceptionThrown() throws Exception {
        // The file contains valid xml data, but does not match the AddressBook class
        StorageFile storage = getStorage("InvalidDuplicateUsernameData.txt", "ValidExamData.txt",
                "InvalidStatistics.txt");
        thrown.expect(StorageOperationException.class);
        storage.load();
    }

    @Test
    public void loadExam_invalidFormat_exceptionThrown() throws Exception {
        // The file contains valid xml data, but does not match the ExamBook class
        StorageFile storage = getStorage("ValidData.txt", "InvalidExamData.txt",
                "ValidStatistics.txt");
        thrown.expect(StorageOperationException.class);
        storage.loadExam();
    }

    @Test
    public void loadStatistics_invalidFormat_exceptionThrown() throws Exception {
        // The file contains valid xml data, but does not match the StatisticsBook class
        StorageFile storage = getStorage("NotValidData.txt", "NotValidExamData.txt",
                "notValidStatisticsData.txt");
        thrown.expect(StorageOperationException.class);
        storage.loadStatistics();
    }

    @Test
    public void load_validFormat() throws Exception {
        List<Pair<String, AddressBook>> inputToExpectedOutputs = new ArrayList<>();
        inputToExpectedOutputs.add(new Pair<>(
                "ValidDataWithNewPassword.txt", getTestAddressBook(false, false)));
        inputToExpectedOutputs.add(new Pair<>(
                "ValidDataWithDefaultPassword.txt", getTestAddressBook(true, false)));
        inputToExpectedOutputs.add(new Pair<>(
                "ValidEmptyData.txt", AddressBook.empty()));
        inputToExpectedOutputs.add(new Pair<>(
                "ValidDataWithAccount.txt", getTestAddressBook(true, true)));
        inputToExpectedOutputs.add(new Pair<>(
                "ValidDataWithoutPassword.txt", getTestAddressBook(true, false)));
        inputToExpectedOutputs.add(new Pair<>(
                "ValidDataWithExam.txt", getTestAddressBook(true, false)));

        for (Pair<String, AddressBook> inputToExpected: inputToExpectedOutputs) {
            final AddressBook actual = getStorage(inputToExpected.getFirst()).load();
            final AddressBook expected = inputToExpected.getSecond();

            // ensure loaded AddressBook is properly constructed with test data
            assertEquals(actual, expected);
            assertEquals(actual.getMasterPassword(), expected.getMasterPassword());
        }
    }
    @Test
    public void load_validFormatIsPerm() throws Exception {
        AddressBook actual = getStorage("ValidDataWithIsPerm.txt").load();
        AddressBook expected = getTestAddressBook();

        // ensure loaded AddressBook is properly constructed with test data
        assertEquals(actual, expected);
        assertEquals(actual.getAllPersons(), expected.getAllPersons());
        assertTrue(actual.isPermAdmin());
    }

    @Test
    public void loadExam_validFormat() throws Exception {
        ExamBook actual = getStorage("ValidData.txt", "ValidExamData.txt",
                "ValidStatisticsData.txt").loadExam();
        ExamBook expected = getTestExamBook();

        // ensure loaded AddressBook is properly constructed with test data
        assertEquals(actual, expected);
    }

    @Test
    public void save_nullAddressBook_exceptionThrown() throws Exception {
        StorageFile storage = getTempStorage();
        thrown.expect(NullPointerException.class);
        storage.save(null);
    }

    @Test
    public void save_nullExamBook_exceptionThrown() throws Exception {
        StorageFile storage = getTempStorage();
        thrown.expect(NullPointerException.class);
        storage.saveExam(null);
    }

    @Test
    public void save_nullStatisticsBook_exceptionThrown() throws Exception {
        StorageFile storage = getTempStorage();
        thrown.expect(NullPointerException.class);
        storage.saveStatistics(null);
    }

    @Test
    public void save_validAddressBook() throws Exception {
        AddressBook ab = getTestAddressBook(true, false);
        ExamBook eb = getTestExamBook();
        StatisticsBook sb = getTestStatisticsBook();
        StorageFile storage = getTempStorage();
        storage.saveExam(eb);
        storage.save(ab);
        storage.saveStatistics(sb);
        // Checks that the password and isPerm is saved as a new field
        assertStorageFilesEqual(storage, getStorage("ValidDataWithDefaultPassword.txt"));

        ab = getTestAddressBook();
        storage = getTempStorage();
        storage.save(ab);

        assertStorageFilesEqual(storage, getStorage("ValidDataWithNewPassword.txt"));
        assertStorageFilesEqual(storage, getStorage("ValidDataWithNewPassword.txt", "ValidExamData.txt",
                "ValidStatisticsData.txt"));

        ab = getTestAddressBook(true, true);
        storage = getTempStorage();
        storage.save(ab);
        assertStorageFilesEqual(storage, getStorage("ValidDataWithAccount.txt"));
    }

    @Test
    public void save_validExamBook() throws Exception {
        ExamBook eb = getTestExamBook();
        AddressBook ab = getTestAddressBook();
        StorageFile storage = getTempStorage();
        storage.saveExam(eb);
        storage.save(ab);
        assertExamsFilesEqual(storage, getStorage("ValidData.txt", "ValidExamData.txt",
                "ValidStatisticsData.txt"));
    }

    @Test
    public void personHasMissingExamInExamBook_exceptionThrown() throws Exception {
        ExamBook eb = getTestExamBook();
        AddressBook ab = getTestAddressBook();
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false);
        final Person john = new Person(new Name("John Doe"),
                new Phone("98765432", false),
                new Email("johnd@gmail.com", false),
                new Address("John street, block 123, #01-01", false),
                Collections.emptySet());
        ab.getAllPersons().find(john).addExam(e1);
        StorageFile storage = getTempStorage();
        thrown.expect(StorageOperationException.class);
        storage.syncAddressBookExamBook(ab, eb);
    }

    @Test
    public void personHasAllExamsInExamBook() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Exam e1 = helper.generateExam(1, false, 0);
        Exam e2 = helper.generateExam(2, true, 2);
        Exam e3 = helper.generateExam(3, false, 1);
        List<Exam> threeExams = helper.generateExamList(e1, e2, e3);
        ExamBook eb = helper.generateExamBook(threeExams);

        Person p1 = helper.generatePerson(1, true, 2, true, 2);
        Person p2 = helper.generatePerson(2, true, 2, true, 2);
        Person p3 = helper.generatePerson(3, true, 3, false, 1);
        List<Person> threePersons = helper.generatePersonList(p1, p2, p3);
        AddressBook ab = helper.generateAddressBook(threePersons);

        StorageFile storage = getTempStorage();
        storage.syncAddressBookExamBook(ab, eb);
    }

    /** Asserts that loading StorageFile will return an Exception with expectedMessage*/
    private void assertReturnsExceptionMessage(StorageFile storage, String expectedMessage) throws Exception {
        try {
            storage.load();
        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
            throw e;
        }
    }

    /**
     * Asserts that the contents of two storage files are the same.
     */
    private void assertStorageFilesEqual(StorageFile sf1, StorageFile sf2) throws Exception {
        assertTextFilesEqual(Paths.get(sf1.getPath()), Paths.get(sf2.getPath()));
    }

    /**
     * Asserts that the contents of two exams files are the same.
     */
    private void assertExamsFilesEqual(StorageFile sf1, StorageFile sf2) throws Exception {
        assertTextFilesEqual(Paths.get(sf1.getPathExam()), Paths.get(sf2.getPathExam()));
    }

    private StorageFile getStorage(String fileName, String examFileName, String statisticsFileName) throws Exception {
        return new StorageFile(TEST_DATA_FOLDER + "/" + fileName, TEST_DATA_FOLDER + "/" + examFileName,
                TEST_DATA_FOLDER + "/" + statisticsFileName);
    }

    private StorageFile getStorage(String fileName) throws Exception {
        return new StorageFile(TEST_DATA_FOLDER + "/" + fileName, VALID_EXAM_DATA_PATH, VALID_STATISTICS_DATA_PATH);
    }
    private StorageFile getTempStorage() throws Exception {
        String tempExam = testFolder.getRoot().getPath() + "/" + "tempExam.txt";
        String tempStatistics = testFolder.getRoot().getPath() + "/" + "tempStatistics.txt";
        return new StorageFile(testFolder.getRoot().getPath() + "/" + "temp.txt", tempExam, tempStatistics);
    }

    private AddressBook getTestAddressBook() throws Exception {
        return getTestAddressBook(false, false);
    }

    private AddressBook getTestAddressBook(boolean isUsingDefaultPassword, boolean hasAccount) throws Exception {
        AddressBook ab = new AddressBook();
        final Person john = new Person(new Name("John Doe"),
                new Phone("98765432", false),
                new Email("johnd@gmail.com", false),
                new Address("John street, block 123, #01-01", false),
                Collections.emptySet());
        Exam exam = new Exam("Math Midterms", "Mathematics", "01-12-2018", "09:00", "10:00", "Held in MPSH", false);
        exam.setTakers(1);
        john.addExam(exam);
        if (hasAccount) {
            john.setAccount(new Account("user", "pw", "Admin"));
        }
        ab.addPerson(john);
        ab.addPerson(new Person(new Name("Betsy Crowe"),
                new Phone("1234567", true),
                new Email("betsycrowe@gmail.com", false),
                new Address("Newgate Prison", true),
                new HashSet<>(Arrays.asList(new Tag("friend"), new Tag("criminal")))));
        if (!isUsingDefaultPassword) {
            ab.setMasterPassword("newPassword");
        }

        return ab;
    }

    private ExamBook getTestExamBook() throws Exception {
        ExamBook eb = new ExamBook();
        eb.addExam(new Exam("Math Midterms", "Mathematics", "01-12-2018", "09:00", "10:00", "Held in MPSH", false));
        eb.addExam(new Exam("English Midterms", "English", "02-12-2018", "09:00", "10:00", "Held in MPSH", false));
        return eb;
    }

    private StatisticsBook getTestStatisticsBook() throws Exception {
        StatisticsBook sb = new StatisticsBook();
        sb.addStatistic(new AssignmentStatistics("Mathematics", "Midterms", "John",
                "72", "102", "4", "98", "95 32", false));
        sb.addStatistic(new AssignmentStatistics("English", "final", "Mark",
                "83", "71", "0", "70", "90 26", true));
        return sb;
    }

}
