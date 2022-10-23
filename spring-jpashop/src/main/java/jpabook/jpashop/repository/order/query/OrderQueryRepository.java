package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jyh1004 on 2022-10-17
 */

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;


	public List<OrderQueryDto> findOrderQueryDtos() {
		List<OrderQueryDto> result = findOrders(); // Query 1번 -> N개
		result.forEach(o -> {
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // Query N번
			o.setOrderItems(orderItems);
		});

		return result;
	}

	public List<OrderQueryDto> findAllByDto_optimization() {
		List<OrderQueryDto> result = findOrders();

		Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

		result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

		return result;
	}

	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		List<OrderItemQueryDto> orderItems = em.createQuery(
				                                       "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
				                                       " join oi.item i" +
				                                       " where oi.order.id in :orderIds",
				                                       OrderItemQueryDto.class)
		                                       .setParameter("orderIds", orderIds)
		                                       .getResultList();

		Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
		                                                       .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
		return orderItemMap;
	}

	private List<Long> toOrderIds(List<OrderQueryDto> result) {
		List<Long> orderIds = result.stream()
		                            .map(o -> o.getOrderId())
		                            .collect(Collectors.toList());
		return orderIds;
	}

	private List<OrderQueryDto> findOrders() {
		return em.createQuery(
				         "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
				         " join o.member m" +
				         " join o.delivery d",
				         OrderQueryDto.class)
		         .getResultList();
	}

	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(
				         "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
				         " join oi.item i" +
				         " where oi.order.id = :orderId",
				         OrderItemQueryDto.class)
		         .setParameter("orderId", orderId)
		         .getResultList();
	}

	public List<OrderFlatDto> findAllByDto_flat() {
		return em.createQuery(
				         "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
				         " from Order o" +
				         " join o.member m" +
				         " join o.delivery d" +
				         " join o.orderItems oi" +
				         " join oi.item i", OrderFlatDto.class)
		         .getResultList();

	}
}