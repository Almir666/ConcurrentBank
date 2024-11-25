import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBank {
    private int count = 0;
    private final List<BankAccount> accounts = new ArrayList<>(); 
    public boolean transfer(BankAccount from, BankAccount to, double amount) {
        if(from.getBalance() <= 0.0 || amount <= 0) {
            return false;
        }
        BankAccount firstLock = from.getId() < to.getId() ? from : to;
        BankAccount secondLock = from.getId() < to.getId() ? to : from;

        firstLock.getLock().lock();
        secondLock.getLock().lock();

        try {
            if(from.withdraw(amount)) {
                to.deposit(amount);
                return true;
            }
            else {
                return false;
            } 
        } finally {
            secondLock.getLock().unlock();
            firstLock.getLock().unlock();
        }
    }

    public BankAccount createAccount(double balance) {
        int id = count + 1;
        count++;
        BankAccount newAccount = new BankAccount(id, balance);
        accounts.add(newAccount);
        return newAccount;
    }

    public double getTotalBalance() {
        double totalBalance = 0.0;
        for(BankAccount acc : accounts) {
            totalBalance += acc.getBalance();
        }
        return totalBalance;
    }



    public static void main(String[] args) {
        ConcurrentBank bank = new ConcurrentBank();

        BankAccount account1 = bank.createAccount(1000);
        BankAccount account2 = bank.createAccount(500);

        Thread transferThread1 = new Thread(() -> bank.transfer(account1, account2, 200));
        Thread transferThread2 = new Thread(() -> bank.transfer(account2, account1, 100));

        transferThread1.start();
        transferThread2.start();

        try {
            transferThread1.join();
            transferThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Total balance: " + bank.getTotalBalance());
        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());
    }

    class BankAccount {
        private double balance;
        private final int id;
        private final ReentrantLock lock = new ReentrantLock();

        public BankAccount(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        public void deposit(double amount) {
            lock.lock();
            try {
                if (amount > 0) {
                    balance += amount;
                }
            } finally {
                lock.unlock();
            }
        }
    
        public boolean withdraw(double amount) {
            lock.lock();
            try {
                if (balance >= amount && amount > 0) {
                    balance -= amount;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        public double getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }

        public ReentrantLock getLock() {
            return lock;
        } 

        public int getId() {
            return id;
        }
    }
}
