package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.Order;
import pkg.order.OrderType;
import pkg.order.BuyOrder;
import pkg.order.SellOrder;

public class Trader {
	// Name of the trader
	String name;
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		// Buy stock straight from the bank
		// Need not place the stock in the order list
		// Add it straight to the user's position
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// Adjust cash possessed since the trader spent money to purchase a
		// stock.
		double price = m.getStockForSymbol(symbol).getPrice();
		if (price * volume > this.cashInHand)
			throw new StockMarketExpection("You don't have enough cash.");
		
		else {
			BuyOrder order = new BuyOrder(symbol, volume, price, this);
			this.position.add(order);
			this.cashInHand -= (price * volume);
		}
		
	}

	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Place a new order and add to the orderlist
		// Also enter the order into the orderbook of the market.
		// Note that no trade has been made yet. The order is in suspension
		// until a trade is triggered.
		Order order;
		if (orderType == OrderType.BUY) {
			// If the stock's price is larger than the cash possessed, then an
			// exception is thrown
			if (price * volume > this.cashInHand)
				throw new StockMarketExpection("You don't have enough cash.");
			for(Order o: this.ordersPlaced) {
				// A trader cannot place two orders for the same stock, throw an
				// exception if there are multiple orders for the same stock.
				if (o.getStockSymbol() == symbol) {
						throw new StockMarketExpection("You've already ordered this stock.");
				}
			}
			order = new BuyOrder(symbol, volume, price, this);
			ordersPlaced.add(order);
		}
		
		else {
			for(Order o: this.position) {
				// Also a person cannot place a sell order for a stock that he does not
				// own. Or he cannot sell more stocks than he possesses. Throw an
				// exception in these cases.
				if (o.getStockSymbol() == symbol) {
					if (o.getSize() < volume)
						throw new StockMarketExpection("You don't have enough stocks.");
				}
			}
			order = new SellOrder(symbol, volume, price, this);
			ordersPlaced.add(order);
		}
		m.addOrder(order);
	}

	public void placeNewMarketOrder(Market m, String symbol, int volume,
		double price, OrderType orderType) throws StockMarketExpection {
		// Similar to the other method, except the order is a market order
		Order order;
		//double priceCalculated = m.getStockForSymbol(symbol).getPrice();
		if (orderType == OrderType.BUY) {
			if (price * volume > this.cashInHand)
				throw new StockMarketExpection("You don't have enough cash.");
			for(Order o: this.ordersPlaced) {
				if (o.getStockSymbol() == symbol) {
						throw new StockMarketExpection("You've already ordered this stock.");
				}
			}
			order = new BuyOrder(symbol, volume, true, this);
			ordersPlaced.add(order);
		}
		
		else {
			for(Order o: this.position) {
				if (o.getStockSymbol() == symbol) {
					if (o.getSize() < volume)
						throw new StockMarketExpection("You don't have enough stocks.");
				}
			}
			order = new SellOrder(symbol, volume, true, this);
		}
		
		
		m.addOrder(order);
	}

	public void tradePerformed(Order o, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		if(o instanceof BuyOrder) {
			if (o.getPrice() < matchPrice && o.getPrice()!=0)
				throw new StockMarketExpection("You aren't willing to trade at this price.");
			boolean added = false;
			for(Order currentOrder: this.position) {
				if (currentOrder.getStockSymbol() == o.getStockSymbol()) {
					currentOrder.setSize(currentOrder.getSize() + o.getSize());
					added = true;
				}		
			}
			
			if (added == false)
				this.position.add(o);
			this.cashInHand -= (o.getSize() * matchPrice);
			this.ordersPlaced.remove(o);
		}
		
		else {
			if (o.getPrice() > matchPrice)
				throw new StockMarketExpection("You aren't willing to sell at this price.");
			for(Order currentOrder: this.position) {
				if (currentOrder.getStockSymbol() == o.getStockSymbol())
					currentOrder.setSize(currentOrder.getSize() - o.getSize());		
			}
			
			this.cashInHand += (o.getSize() * matchPrice);
			this.ordersPlaced.remove(o);			
		}

		
	}
	
	public String getName() {
		return  this.name;
	}
	
	public double getCashInHand(){
		return this.cashInHand;
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order o : position) {
			o.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order o : ordersPlaced) {
			o.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
