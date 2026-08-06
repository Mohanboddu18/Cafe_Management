export interface User {
  id?: number;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  role: string;
  token?: string;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  imageUrl: string;
  displayOrder: number;
  active: boolean;
}

export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  prepTimeMins: number;
  isVeg: boolean;
  isAvailable: boolean;
  isFeatured: boolean;
  categoryId?: number;
  category?: Category;
  averageRating?: number;
  totalRatings?: number;
}

export interface ItemRatingRequest {
  menuItemId: number;
  rating: number;
  comment?: string;
}

export interface SubmitReviewRequest {
  orderId: number;
  customerName?: string;
  ratings: ItemRatingRequest[];
}

export interface ReviewResponse {
  id: number;
  menuItemId: number;
  menuItemName: string;
  rating: number;
  comment?: string;
  customerName?: string;
  createdAt: string;
}

export interface RestaurantTable {
  id: number;
  tableNumber: number;
  capacity: number;
  status: 'AVAILABLE' | 'OCCUPIED' | 'BILL_REQUESTED' | 'RESERVED';
  qrCodeUrl: string;
  qrToken: string;
  currentTokenSerial?: string;
  currentSessionId?: string;
}

export interface CartItem {
  id?: number;
  menuItem: MenuItem;
  quantity: number;
  notes?: string;
}

export interface Cart {
  id?: number;
  sessionId: string;
  tableId: number;
  items: CartItem[];
}

export interface OrderItem {
  id?: number;
  menuItemId: number;
  menuItemName: string;
  imageUrl?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  itemStatus?: string;
  notes?: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  tableId: number;
  tableNumber: number;
  customerName: string;
  customerTokenSerial?: string;
  sessionId?: string;
  totalAmount: number;
  discountAmount: number;
  taxAmount: number;
  netAmount: number;
  status: 'NEW' | 'ACCEPTED' | 'PREPARING' | 'READY' | 'SERVED' | 'BILL_REQUESTED' | 'COMPLETED' | 'PAID' | 'CANCELLED';
  notes?: string;
  orderTime: string;
  items: OrderItem[];
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  orderId: number;
  orderNumber: string;
  tableNumber: number;
  subtotal: number;
  discount: number;
  couponCode?: string;
  gstAmount: number;
  totalPayable: number;
  paymentMethod: string;
  paymentStatus: string;
  pdfUrl?: string;
  createdAt: string;
  items: OrderItem[];
}

export interface Coupon {
  id?: number;
  code: string;
  description: string;
  discountType: 'PERCENTAGE' | 'FLAT';
  discountValue: number;
  minOrderAmount: number;
  maxDiscount: number;
  validUntil: string;
  active: boolean;
}

export interface Inventory {
  id?: number;
  itemName: string;
  unit: string;
  currentStock: number;
  minRequiredStock: number;
  costPerUnit: number;
}

export interface StockHistory {
  id?: number;
  inventoryId: number;
  transactionType: 'IN' | 'OUT' | 'ADJUSTMENT';
  quantity: number;
  notes: string;
  createdAt?: string;
}

export interface NotificationMsg {
  id: number;
  targetRole: string;
  title: string;
  message: string;
  orderId?: number;
  tableId?: number;
  isRead: boolean;
  createdAt: string;
}

export interface DashboardSummary {
  todaysSales: number;
  weeklySales: number;
  monthlySales: number;
  activeOrdersCount: number;
  totalTablesCount: number;
  occupiedTablesCount: number;
  popularItems: { itemName: string; quantitySold: number }[];
  dailyRevenueChart: { [key: string]: number };
  recentOrders: { orderNumber: string; tableNumber: number; amount: number; status: string; time: string }[];
}
