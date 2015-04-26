package pkg.order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;

import pkg.market.Market;
import pkg.market.api.PriceSetter;
import pkg.exception.StockMarketExpection;
//import pkg.util.OrderUtility;

public class OrderBook {
	Market m;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public OrderBook(Market m) {
		this.m = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		// Populate the buyOrders and sellOrders data structures, whichever
		// appropriate
		if (order instanceof BuyOrder){
			if (!buyOrders.containsKey(order.getStockSymbol())){
				ArrayList<Order> OrderList = new ArrayList<Order>();
				OrderList.add(order);
				buyOrders.put(order.getStockSymbol(),OrderList);
			}
			else{
				buyOrders.get(order.getStockSymbol()).add(order);
			}
		}
		else{
			if (!sellOrders.containsKey(order.getStockSymbol())){
				ArrayList<Order> OrderList = new ArrayList<Order>();
				OrderList.add(order);
				sellOrders.put(order.getStockSymbol(),OrderList);
			}
			else{
				sellOrders.get(order.getStockSymbol()).add(order);
			}
		}
	}

	public void trade() {
		// Complete the trading.
		// 1. Follow and create the orderbook data representation (see spec)
		for (Map.Entry<String, ArrayList<Order>> entry: this.buyOrders.entrySet()){
			ArrayList<Order> buyOrdersForStock = entry.getValue();
			String stockName = entry.getKey();
			ArrayList<Order> sellOrdersForStock = this.sellOrders.get(stockName);
			
			int maxVolume = 0;
			double matchPrice = 0;
			Collections.sort(buyOrdersForStock, new buyOrderComparator());
			Collections.sort(sellOrdersForStock, new sellOrderComparator());
			int buyOrderVolume = 0;
			int sellOrderVolume = 0;
			int sizeForPrice0 = 0;
			
			for (Order buyOrder: buyOrdersForStock){
				if (buyOrder.getPrice() == 0){
					sizeForPrice0 = buyOrder.getSize();
					buyOrderVolume = sizeForPrice0;
				}
			}
			for (Order buyOrder: buyOrdersForStock){
				if (buyOrder.getPrice()!=0){
					buyOrderVolume += buyOrder.getSize();
				}
				sellOrderVolume = 0;
				for(Order sellOrder: sellOrdersForStock){
					sellOrderVolume += sellOrder.getSize();
					// 2. Find the matching price
					if (buyOrder.getPrice() == sellOrder.getPrice()){
						
						if (maxVolume < Math.min(sellOrderVolume, buyOrderVolume)){
							matchPrice = sellOrder.getPrice();
							maxVolume = Math.min(sellOrderVolume, buyOrderVolume);
						}
					}
					
				}
			}
			// 3. Update the stocks price in the market using the PriceSetter.
			// Note that PriceSetter follows the Observer pattern. Use the pattern.
			PriceSetter setPrice = new PriceSetter();
			setPrice.registerObserver(m.getMarketHistory());
			m.getMarketHistory().setSubject(setPrice);
			setPrice.setNewPrice(m, stockName, matchPrice);
			
			// 5. Delegate to trader that the trade has been made, so that the
			// trader's orders can be placed to his possession (a trader's position
			// is the stocks he owns)
			// (Add other methods as necessary)
			for(Order order:buyOrdersForStock) {
				try {
					order.getTrader().tradePerformed(order, matchPrice);
				}
				
				catch(StockMarketExpection e) {
					//do nothing
				}
			}

			for(Order order:sellOrdersForStock) {
				try {
					order.getTrader().tradePerformed(order, matchPrice);
				}
				
				catch(StockMarketExpection e) {
					//do nothing
				}
			}
			
			// 4. Remove the traded orders from the orderbook
			//OrderUtility.findAndExtractOrder(buyOrdersForStock, stockName);
			Iterator<Order> buyIterator = buyOrdersForStock.iterator();
			Iterator<Order> sellIterator = sellOrdersForStock.iterator();
			int restVolume = maxVolume-sizeForPrice0;
			Order buyOrder;
			Order sellOrder;
			while (buyIterator.hasNext()) {
				buyOrder = buyIterator.next();
				if(buyOrder.getPrice()==0.0){
					buyIterator.remove();
				}
				if (restVolume-buyOrder.getSize() >= 0) {
					restVolume -= buyOrder.getSize();
					buyIterator.remove();
					
				}
				
				else if (restVolume > 0) {
					buyOrder.setSize(buyOrder.getSize()-restVolume);
					restVolume = 0;
				}
			}
			
			restVolume = maxVolume;
			while (sellIterator.hasNext()) {
				sellOrder = sellIterator.next();
				if (restVolume-sellOrder.getSize() >= 0) {
					restVolume -= sellOrder.getSize();
					sellIterator.remove();
					
				}
				
				else if (restVolume > 0) {
					sellOrder.setSize(sellOrder.getSize()-restVolume);
					restVolume = 0;
				}
			}
		}
		
	
	}

}

class sellOrderComparator implements Comparator<Order>{
	@Override
	public int compare(Order o1, Order o2){
		Double o1Value = o1.getPrice();
		Double o2Value = o2.getPrice();
		return o1Value.compareTo(o2Value);
	}
}

class buyOrderComparator implements Comparator<Order>{
	@Override
	public int compare(Order o1, Order o2){
		Double o1Value = o1.getPrice();
		Double o2Value = o2.getPrice();
		return o2Value.compareTo(o1Value);
	}
}
