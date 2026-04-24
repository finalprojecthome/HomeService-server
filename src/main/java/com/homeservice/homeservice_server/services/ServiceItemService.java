package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.service.ServiceItemResponse;
import com.homeservice.homeservice_server.entities.Category;
import com.homeservice.homeservice_server.entities.ServiceItem;
import com.homeservice.homeservice_server.entities.SubService;
import com.homeservice.homeservice_server.repositories.ServiceItemRepository;
import com.homeservice.homeservice_server.repositories.SubServiceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ServiceItemService {
	private static final NumberFormat TH_PRICE = NumberFormat.getNumberInstance(Locale.forLanguageTag("th-TH"));
	private static final Collator TH_COLLATOR = Collator.getInstance(Locale.forLanguageTag("th"));

	static {
		TH_PRICE.setMinimumFractionDigits(2);
		TH_PRICE.setMaximumFractionDigits(2);
	}

	private final ServiceItemRepository serviceItemRepository;
	private final SubServiceRepository subServiceRepository;

	public ServiceItemService(ServiceItemRepository serviceItemRepository, SubServiceRepository subServiceRepository) {
		this.serviceItemRepository = serviceItemRepository;
		this.subServiceRepository = subServiceRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceItemResponse> getServices() {
		List<ServiceItem> items = new ArrayList<>(serviceItemRepository.findAllWithCategoryAndSubServices());
		items.sort(Comparator
				.comparingInt(ServiceItemService::categorySortOrder)
				.thenComparing(s -> s.getName() == null ? "" : s.getName(), Comparator.nullsLast(TH_COLLATOR)));

		return items.stream().map(this::toResponse).toList();
	}

	private static int categorySortOrder(ServiceItem s) {
		Category c = s.getCategory();
		return c != null && c.getSortOrder() != null ? c.getSortOrder() : 0;
	}

	private ServiceItemResponse toResponse(ServiceItem s) {
		String categoryLabel = "ไม่ระบุหมวด";
		Category cat = s.getCategory();
		if (cat != null && cat.getName() != null) {
			String n = cat.getName().trim();
			if (!n.isEmpty()) {
				categoryLabel = n;
			}
		}

		List<SubService> subServices = subServiceRepository.findByServiceItem_ServiceId(s.getServiceId());
		String priceLabel = formatPriceRange(subServices);
		String image = s.getImageUrl() != null ? s.getImageUrl().trim() : "";

		return ServiceItemResponse.builder()
				.id(String.valueOf(s.getServiceId()))
				.title(s.getName() != null ? s.getName() : "")
				.category(categoryLabel)
				.price(priceLabel)
				.imageSrc(image)
				.build();
	}

	private String formatPriceRange(List<SubService> subServices) {
		if (subServices == null || subServices.isEmpty()) {
			return "—";
		}
		List<BigDecimal> prices = new ArrayList<>();
		for (SubService sub : subServices) {
			if (sub.getPricePerUnit() != null) {
				prices.add(sub.getPricePerUnit());
			}
		}
		if (prices.isEmpty()) {
			return "—";
		}
		BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		if (min.compareTo(max) == 0) {
			return TH_PRICE.format(min);
		}
		return TH_PRICE.format(min) + " - " + TH_PRICE.format(max);
	}
}
