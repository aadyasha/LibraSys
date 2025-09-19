import java.util.*;

// ---------- Policy (Fine System) ----------
interface FinePolicy {
    int calculate(int days);   // calculate fine based on days
}

// Basic fine = per day * rate
class SimpleFine implements FinePolicy {
    int rate;

    SimpleFine(int r) {
        rate = r;
    }

    public int calculate(int days) {
        return rate * days;
    }
}

// ---------- Core Item ----------
abstract class LibraryItem {
    private static int idGen = 1000;  // generates IDs starting at 1000
    private int id = idGen++;         // auto assign ID

    String title, author;
    boolean available = true;

    int borrowDays;
    int borrowCount = 0;

    LibraryItem(String t, String a) {
        title = t;
        author = a;
    }

    int getId() { return id; }
    String getTitle() { return title; }
    boolean isAvailable() { return available; }
    int getBorrowCount() { return borrowCount; }

    // borrow process
    void borrow(int days, FinePolicy p, FineManager m) {
        if (!available) {
            notAvailableMsg();
            return;
        }
        available = false;
        borrowDays = days;
        borrowCount++;

        System.out.println("\"" + title + "\" borrowed for " + days + " day(s). (ID=" + id + ")");
        System.out.println("Due in " + days + " days | Fine/day = Rs." + p.calculate(1));

        int fineTotal = p.calculate(days);
        m.addFine(id, fineTotal);
    }

    void returnItem(FineManager m) {
        if (!available) {
            if (borrowDays < 0) { 
                // overdue → extra fine of Rs.30
                System.out.println(" \"" + title + "\" is overdue! Extra fine Rs.30 applied.");
                m.addFine(id, 30);
            }
            available = true;
            borrowDays = 0;
            System.out.println(" Returned \"" + title + "\" (ID=" + id + ")");
        }
    }

    void notAvailableMsg() {
        System.out.println("\"" + title + "\" not available now. Will be back soon! ");
    }

    // simulate passage of one day
    void nextDay() {
        if (!available) {
            borrowDays--;
        }
    }

    public String toString() {
        String status;
        if (available) {
            status = "Available";
        } else {
            status = borrowDays >= 0 ? "Borrowed (" + borrowDays + " days left)" 
                                     : " Overdue!";
        }
        return "[" + getClass().getSimpleName() + "] \"" + title + "\" — " + author + 
               " | ID=" + id + " | Status: " + status;
    }
}

// ---------- Item Types ----------
class Book extends LibraryItem {
    int pages;

    Book(String t, String a, int p) {
        super(t, a);
        pages = p;
    }

    public String toString() {
        return super.toString() + " | " + pages + "p";
    }
}

interface Playable { 
    void playSample(); 
}

class AudioBook extends LibraryItem implements Playable {
    double hrs;

    AudioBook(String t, String a, double h) {
        super(t, a);
        hrs = h;
    }

    public void playSample() {
        System.out.println(" Playing sample: " + getTitle());
    }

    public String toString() {
        return super.toString() + " | " + hrs + "hrs";
    }
}

class EMagazine extends LibraryItem {
    int issue;

    EMagazine(String t, String a, int i) {
        super(t, a);
        issue = i;
    }

    void archive() {
        System.out.println("📰 Archiving Issue #" + issue + " of " + getTitle());
    }

    public String toString() {
        return super.toString() + " | Issue " + issue;
    }
}

// ---------- Fine Manager ----------
class FineManager {
    Map<Integer, Integer> fines = new HashMap<>();
    int totalPurchase = 0;

    void addFine(int id, int amt) {
        fines.put(id, fines.getOrDefault(id, 0) + amt);
    }

    void addPurchase(int amt) {
        totalPurchase += amt;
    }

    void showFines() {
        System.out.println("\n Fine Records:");
        if (fines.isEmpty()) {
            System.out.println("No fines yet");
            return;
        }
        for (var e : fines.entrySet()) {
            System.out.println("Item " + e.getKey() + " -> Rs." + e.getValue());
        }
    }

    void showStats(List<LibraryItem> items) {
        System.out.println("\n=====  Library Stats =====");

        // find most borrowed
        LibraryItem max = null;
        for (LibraryItem it : items) {
            if (max == null || it.getBorrowCount() > max.getBorrowCount()) {
                max = it;
            }
        }

        if (max != null) {
            System.out.println("Most Borrowed: \"" + max.getTitle() + "\" (" + max.getBorrowCount() + " times)");
        }

        int totalFines = 0;
        for (int fine : fines.values()) {
            totalFines += fine;
        }

        System.out.println("Total Fines Collected: Rs." + totalFines);
        System.out.println("Total Purchases: Rs." + totalPurchase);
    }
}

// ---------- Main ----------
public class main {
    static Scanner sc = new Scanner(System.in);

    static void search(List<LibraryItem> items, FinePolicy p, FineManager m) {
        System.out.print(" Keyword: ");
        sc.nextLine(); // flush buffer
        String k = sc.nextLine().toLowerCase();

        boolean found = false;

        for (LibraryItem it : items) {
            if (it.getTitle().toLowerCase().contains(k) || it.author.toLowerCase().contains(k)) {
                found = true;
                System.out.println(it);

                if (it.isAvailable()) {
                    System.out.print("👉 (1)Borrow (2)Buy: ");
                    int c = sc.nextInt();

                    if (c == 1) {
                        System.out.print("Days: ");
                        int days = sc.nextInt();
                        it.borrow(days, p, m);
                    } else {
                        System.out.print(" Price: ");
                        int price = sc.nextInt();
                        m.addPurchase(price);
                        it.available = false;
                        System.out.println("Bought \"" + it.getTitle() + "\" (ID=" + it.getId() + ")");
                    }
                } else {
                    it.notAvailableMsg();
                }
            }
        }
        if (!found) System.out.println("No matches found!");
    }

    // move to next day (simulate overdue)
    static void nextDay(List<LibraryItem> items) {
        for (LibraryItem it : items) {
            it.nextDay();
        }
        System.out.println(" A new day has passed. Borrowed items updated.");
    }

    // show only borrowed items
    static void showBorrowed(List<LibraryItem> items) {
        System.out.println("\n===== Borrowed Items =====");
        boolean any = false;
        for (LibraryItem it : items) {
            if (!it.isAvailable()) {
                System.out.println(it);
                any = true;
            }
        }
        if (!any) {
            System.out.println("No items are currently borrowed ");
        }
    }

    public static void main(String[] args) {
        FinePolicy policy = new SimpleFine(10);
        FineManager manager = new FineManager();

        List<LibraryItem> libraryList = new ArrayList<>();
        libraryList.add(new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", 309));
        libraryList.add(new AudioBook("Becoming", "Michelle Obama", 19.5));
        libraryList.add(new EMagazine("National Geographic", "Various", 202));

        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1.Show all items");
            System.out.println("2.Search items");
            System.out.println("3.Return item");
            System.out.println("4.Show Reports");
            System.out.println("5.Next Day (simulate)");
            System.out.println("6.Show Borrowed Items");
            System.out.println("7.Exit");
            System.out.print(" Choice: ");
            int ch = sc.nextInt();

            switch (ch) {
                case 1:
                    for (LibraryItem i : libraryList) {
                        System.out.println(i);
                    }
                    break;

                case 2:
                    search(libraryList, policy, manager);
                    break;

                case 3:
                    System.out.print("ID to return: ");
                    int id = sc.nextInt();

                    boolean returned = false;
                    for (LibraryItem item : libraryList) {
                        if (item.getId() == id) {
                            item.returnItem(manager);
                            returned = true;
                            break;
                        }
                    }
                    if (!returned) System.out.println("Invalid ID");
                    break;

                case 4:
                    manager.showFines();
                    manager.showStats(libraryList);
                    break;

                case 5:
                    nextDay(libraryList);
                    break;

                case 6:
                    showBorrowed(libraryList);
                    break;

                case 7:
                    String[] exits = {
                        "Have a great reading journey!",
                        "See you next time!",
                        "Keep turning the pages!",
                        "Happy Listening!",
                        "Stay updated with great reads!",
                        "Knowledge is power — keep exploring!"
                    };
                    int choice = new Random().nextInt(exits.length);
                    System.out.println("\n" + exits[choice] + "\n");
                    return;
            }
        }
    }
}
