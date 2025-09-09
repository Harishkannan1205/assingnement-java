package assignment;

import java.util.*;
import java.time.LocalDate;

public class RMAppMain {

    // -------- Inner Class: Order --------
    static class Order {
        private String orderId;
        private String buyer;
        private List<String> items;
        private double totalAmount;
        private String purchaseDate;

        public Order(String orderId, String buyer, List<String> items, double totalAmount, String purchaseDate) {
            this.orderId = orderId;
            this.buyer = buyer;
            this.items = items;
            this.totalAmount = totalAmount;
            this.purchaseDate = purchaseDate;
        }

        public String getOrderId() { return orderId; }
        public double getTotalAmount() { return totalAmount; }

        public void displayOrder() {
            System.out.println("Order ID: " + orderId + " | Buyer: " + buyer + " | Amount: " + totalAmount);
        }
    }

    // -------- Inner Class: ReturnRequest --------
    static class ReturnRequest {
        private String rmaId;
        private String orderId;
        private String reason;
        private String condition;
        private String status; // REQUESTED, APPROVED, DENIED

        public ReturnRequest(String rmaId, String orderId, String reason, String condition) {
            this.rmaId = rmaId;
            this.orderId = orderId;
            this.reason = reason;
            this.condition = condition;
            this.status = "REQUESTED";
        }

        public String getRmaId() { return rmaId; }
        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }

        public void approve() { this.status = "APPROVED"; }
        public void deny() { this.status = "DENIED"; }

        public void displayRequest() {
            System.out.println("RMA ID: " + rmaId + " | Order: " + orderId +
                    " | Reason: " + reason + " | Status: " + status);
        }
    }

    // -------- Inner Class: Refund (Base) --------
    static class Refund {
        protected String refundId;
        protected String rmaId;
        protected String method;
        protected double amount;
        protected LocalDate processedDate;

        public Refund(String refundId, String rmaId, String method, double amount) {
            this.refundId = refundId;
            this.rmaId = rmaId;
            this.method = method;
            this.amount = amount;
        }

        public void processRefund() {
            processedDate = LocalDate.now();
            System.out.println("Refund " + refundId + " processed for RMA " + rmaId +
                    " | Amount: " + amount);
        }
    }

    // -------- Inner Class: CardRefund --------
    static class CardRefund extends Refund {
        private String cardNumber;

        public CardRefund(String refundId, String rmaId, double amount, String cardNumber) {
            super(refundId, rmaId, "Card", amount);
            this.cardNumber = cardNumber;
        }

        @Override
        public void processRefund() {
            super.processRefund();
            System.out.println("Refund credited back to Card: " + cardNumber);
        }
    }

    // -------- Inner Class: WalletRefund --------
    static class WalletRefund extends Refund {
        private String walletId;

        public WalletRefund(String refundId, String rmaId, double amount, String walletId) {
            super(refundId, rmaId, "Wallet", amount);
            this.walletId = walletId;
        }

        @Override
        public void processRefund() {
            super.processRefund();
            System.out.println("Refund added to Wallet: " + walletId);
        }
    }

    // -------- Inner Class: RMAService --------
    static class RMAService {
        private List<Order> orders = new ArrayList<>();
        private List<ReturnRequest> requests = new ArrayList<>();
        private List<Refund> refunds = new ArrayList<>();

        public void addOrder(Order order) {
            orders.add(order);
        }

        // Overloaded createRMA
        public ReturnRequest createRMA(String rmaId, String orderId, String reason, String condition) {
            ReturnRequest req = new ReturnRequest(rmaId, orderId, reason, condition);
            requests.add(req);
            return req;
        }

        public ReturnRequest createRMA(String rmaId, String orderId, String reason, String condition, String photos) {
            ReturnRequest req = new ReturnRequest(rmaId, orderId, reason + " | Photo: " + photos, condition);
            requests.add(req);
            return req;
        }

        public void inspectAndApprove(ReturnRequest req, boolean approved) {
            if (approved) req.approve();
            else req.deny();
        }

        public Refund processRefund(ReturnRequest req, String type) {
            if (!req.getStatus().equals("APPROVED")) {
                System.out.println("RMA not approved. Refund denied.");
                return null;
            }

            Order order = orders.stream()
                    .filter(o -> o.getOrderId().equals(req.getOrderId()))
                    .findFirst()
                    .orElse(null);

            if (order == null) return null;

            Refund refund;
            if (type.equalsIgnoreCase("Card")) {
                refund = new CardRefund("RF" + (refunds.size() + 1),
                        req.getRmaId(), order.getTotalAmount(), "XXXX-1234");
            } else {
                refund = new WalletRefund("RF" + (refunds.size() + 1),
                        req.getRmaId(), order.getTotalAmount(), "Wallet001");
            }

            refund.processRefund();
            refunds.add(refund);
            return refund;
        }

        public void showSummary() {
            System.out.println("\n--- RMA & Refund Summary ---");
            requests.forEach(ReturnRequest::displayRequest);
            refunds.forEach(r -> System.out.println("RefundID: " + r.refundId + " | Method: " + r.method));
        }
    }

    // -------- MAIN METHOD --------
    public static void main(String[] args) {
        RMAService service = new RMAService();

        // Create Orders
        Order o1 = new Order("O101", "Alice",
                Arrays.asList("Laptop", "Mouse"), 50000, "2025-08-01");
        Order o2 = new Order("O102", "Bob",
                Arrays.asList("Headphones"), 2000, "2025-08-05");

        service.addOrder(o1);
        service.addOrder(o2);

        // Create RMA (Return Requests)
        ReturnRequest r1 = service.createRMA("RMA01", "O101", "Defective product", "Used");
        ReturnRequest r2 = service.createRMA("RMA02", "O102", "Not working", "New", "photo.jpg");

        // Inspect & Approve/Reject
        service.inspectAndApprove(r1, true);   // approve
        service.inspectAndApprove(r2, false);  // deny

        // Process Refunds
        service.processRefund(r1, "Card");
        service.processRefund(r2, "Wallet"); // denied, no refund

        // Show Summary
        service.showSummary();
    }
}
