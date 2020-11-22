package database;

import java.util.Comparator;

/**
 * Classes for comparing vocabulary.
 * Implemented as Singleton to save resources.
 *
 * Supports comparing with eng, add_time
 * Used in SeeAllFragment to sort the RecyclerView with specific criteria.
 */
public class VocaComparator {

    private static AddedTimeComparator addedTimeComparator;
    private static EngComparator engComparator;

    public static AddedTimeComparator getAddedTimeComparator() {
        if (addedTimeComparator == null) {
            synchronized (AddedTimeComparator.class) {
                addedTimeComparator = new AddedTimeComparator();
            }
        }
        return addedTimeComparator;
    }

    public static EngComparator getEngComparator() {
        if (engComparator == null) {
            synchronized (EngComparator.class) {
                engComparator = new EngComparator();
            }
        }
        return engComparator;
    }

    private static class AddedTimeComparator implements Comparator<Vocabulary> { // last added first
        @Override
        public int compare(Vocabulary o1, Vocabulary o2) {
            return o2.addedTime - o1.addedTime;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AddedTimeComparator) {
                return this == (AddedTimeComparator) obj;
            }
            return false;
        }
    }

    private static class EngComparator implements Comparator<Vocabulary> {
        @Override
        public int compare(Vocabulary o1, Vocabulary o2) {
            return o1.eng.compareTo(o2.eng);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EngComparator) {
                return this == (EngComparator) obj;
            }
            return false;
        }
    }
}
