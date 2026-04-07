package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.ServiceCatalogItemResponse;
import com.homeservice.homeservice_server.entities.Category;
import com.homeservice.homeservice_server.entities.ServiceItem;
import com.homeservice.homeservice_server.entities.SubService;
import com.homeservice.homeservice_server.repositories.ServiceItemRepository;
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
public class ServiceCatalogService {
	private static final NumberFormat TH_PRICE = NumberFormat.getNumberInstance(Locale.forLanguageTag("th-TH"));
	private static final Collator TH_COLLATOR = Collator.getInstance(Locale.forLanguageTag("th"));

	static {
		TH_PRICE.setMinimumFractionDigits(2);
		TH_PRICE.setMaximumFractionDigits(2);
	}

	private final ServiceItemRepository serviceItemRepository;

	public ServiceCatalogService(ServiceItemRepository serviceItemRepository) {
		this.serviceItemRepository = serviceItemRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceCatalogItemResponse> listServices() {
		List<ServiceItem> items = new ArrayList<>(serviceItemRepository.findAllWithCategoryAndSubServices());
		items.sort(Comparator
				.comparingInt(ServiceCatalogService::categorySortOrder)
				.thenComparing(s -> s.getName() == null ? "" : s.getName(), Comparator.nullsLast(TH_COLLATOR)));

		return items.stream().map(this::toResponse).toList();
	}

	private static int categorySortOrder(ServiceItem s) {
		Category c = s.getCategory();
		return c != null && c.getSortOrder() != null ? c.getSortOrder() : 0;
	}

	private ServiceCatalogItemResponse toResponse(ServiceItem s) {
		String categoryLabel = "ไม่ระบุหมวด";
		Category cat = s.getCategory();
		if (cat != null && cat.getName() != null) {
			String n = cat.getName().trim();
			if (!n.isEmpty()) {
				categoryLabel = n;
			}
		}

		String priceLabel = formatPriceRange(s.getSubServices());
		String image = s.getImageUrl() != null ? s.getImageUrl().trim() : "";

		return new ServiceCatalogItemResponse(
				String.valueOf(s.getServiceId()),
				s.getName() != null ? s.getName() : "",
				categoryLabel,
				priceLabel,
				image
		);
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
