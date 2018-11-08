package seedu.addressbook.commands.commandformat.indexformat;

import static seedu.addressbook.ui.Gui.DISPLAYED_INDEX_OFFSET;

import java.util.HashMap;

import seedu.addressbook.commands.Command;
import seedu.addressbook.data.person.Assessment;
import seedu.addressbook.data.person.Exam;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyExam;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.data.person.UniqueExamList.ExamNotFoundException;
import seedu.addressbook.data.person.UniquePersonList.PersonNotFoundException;

/** The abstract class for commands with the format of KEYWORD ... KEYWORD */
public abstract class IndexFormatCommand extends Command {
    private HashMap<ObjectTargeted, Integer> targetMap = new HashMap<>();

    /**
     * Extracts the the target (immutable) person in the last shown list from the given arguments.
     *
     * @throws IndexOutOfBoundsException if the target index is out of bounds of the last viewed listing
     */
    protected ReadOnlyPerson getTargetReadOnlyPerson() throws IndexOutOfBoundsException {
        return relevantPersons.get(targetMap.get(ObjectTargeted.PERSON) - DISPLAYED_INDEX_OFFSET);
    }

    /**
     * Extracts the the target (mutable) person in the last shown list from the given arguments.
     *
     * @throws IndexOutOfBoundsException if the target index is out of bounds of the last viewed listing
     * @throws PersonNotFoundException if the target person cannot be found in the address book
     */
    protected Person getTargetPerson() throws IndexOutOfBoundsException, PersonNotFoundException {
        return addressBook.findPerson(getTargetReadOnlyPerson());
    }

    /**
     * Extracts the the target assessment in the last shown list from the given arguments.
     */
    protected Assessment getTargetAssessment() throws AssessmentIndexOutOfBoundsException {
        try {
            return relevantAssessments.get(targetMap.get(ObjectTargeted.ASSESSMENT) - DISPLAYED_INDEX_OFFSET);
        } catch (IndexOutOfBoundsException iob) {
            throw new AssessmentIndexOutOfBoundsException(iob.getMessage());
        }
    }

    /**
     * Extracts the target (mutable) exam in the last shown exam list from the given arguments.
     *
     * @throws ExamIndexOutOfBoundsException if the target exam index is out of bounds of the last viewed exam listing
     * @throws ExamNotFoundException if the target exam cannot be found in the exam book
     */
    protected Exam getTargetExam() throws ExamIndexOutOfBoundsException, ExamNotFoundException {
        return examBook.findExam(getTargetReadOnlyExam());
    }

    /**
     * Extracts the the target (immutable) exam in the last shown exam list from the given arguments.
     *
     * @throws ExamIndexOutOfBoundsException if the target exam index is out of bounds of the last viewed exam listing
     */
    protected ReadOnlyExam getTargetReadOnlyExam() throws ExamIndexOutOfBoundsException {
        try {
            return relevantExams.get(targetMap.get(ObjectTargeted.EXAM) - DISPLAYED_INDEX_OFFSET);
        } catch (IndexOutOfBoundsException e) {
            throw new ExamIndexOutOfBoundsException(e.getMessage());
        }

    }

    public void setTargetIndex(int targetIndex, ObjectTargeted objectTargeted) {
        targetMap.put(objectTargeted, targetIndex);
    }

    public int getTargetIndex(ObjectTargeted objectTargeted) {
        return targetMap.get(objectTargeted);
    }
}
