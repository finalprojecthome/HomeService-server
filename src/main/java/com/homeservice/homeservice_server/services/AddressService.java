package com.homeservice.homeservice_server.services;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.homeservice.homeservice_server.dto.address.DistrictResponse;
import com.homeservice.homeservice_server.dto.address.ProvinceResponse;
import com.homeservice.homeservice_server.dto.address.SubDistrictResponse;
import com.homeservice.homeservice_server.dto.user.AddressRequest;
import com.homeservice.homeservice_server.dto.user.GetAddressResponse;
import com.homeservice.homeservice_server.entities.Address;
import com.homeservice.homeservice_server.entities.District;
import com.homeservice.homeservice_server.entities.Province;
import com.homeservice.homeservice_server.entities.SubDistrict;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.AddressRepository;
import com.homeservice.homeservice_server.repositories.DistrictRepository;
import com.homeservice.homeservice_server.repositories.ProvinceRepository;
import com.homeservice.homeservice_server.repositories.SubDistrictRepository;
import com.homeservice.homeservice_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

	private final AddressRepository addressRepository;
	private final ProvinceRepository provinceRepository;
	private final DistrictRepository districtRepository;
	private final SubDistrictRepository subDistrictRepository;
	private final UserRepository userRepository;

	private ProvinceResponse toProvinceResponse(Province province) {
		return ProvinceResponse.builder()
				.id(province.getProvinceId())
				.name(province.getName())
				.build();
	}

	private DistrictResponse toDistrictResponse(District district) {
		return DistrictResponse.builder()
				.id(district.getDistrictId())
				.name(district.getName())
				.build();
	}

	private SubDistrictResponse toSubDistrict(SubDistrict subDistrict) {
		return SubDistrictResponse.builder()
				.id(subDistrict.getSubDistrictId())
				.name(subDistrict.getName())
				.latitude(subDistrict.getLatitude())
				.longitude(subDistrict.getLongitude())
				.postCode(subDistrict.getPostCode())
				.build();
	}

	private GetAddressResponse toGetAddressResponse(Address address) {
		SubDistrict subDistrict = address.getSubDistrict();
		District district = subDistrict.getDistrict();
		Province province = district.getProvince();
		return GetAddressResponse.builder()
				.id(address.getAddressId())
				.addressName(address.getAddressName())
				.addressDetail(address.getAddressDetail())
				.province(GetAddressResponse.AreaInfo.builder()
						.id(province.getProvinceId())
						.name(province.getName())
						.build())
				.district(GetAddressResponse.AreaInfo.builder()
						.id(district.getDistrictId())
						.name(district.getName())
						.build())
				.subDistrict(GetAddressResponse.AreaInfo.builder()
						.id(subDistrict.getSubDistrictId())
						.name(subDistrict.getName())
						.build())
				.postCode(subDistrict.getPostCode())
				.latitude(address.getLatitude())
				.longitude(address.getLongitude())
				.build();
	}

	public List<ProvinceResponse> getProvinces() {
		return provinceRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(Province::getName))
				.map(this::toProvinceResponse)
				.toList();
	}

	public List<DistrictResponse> getDistrictsByProvinceId(Integer provinceId) {
		if (!provinceRepository.existsById(provinceId)) {
			throw new NotFoundException("ไม่พบจังหวัด");
		}
		return districtRepository.findByProvince_ProvinceId(provinceId)
				.stream()
				.sorted(Comparator.comparing(District::getName))
				.map(this::toDistrictResponse)
				.toList();
	}

	public List<SubDistrictResponse> getSubDistrictsByDistrictId(Integer districtId) {
		if (!districtRepository.existsById(districtId)) {
			if (districtId >= 1100) {
				throw new NotFoundException("ไม่พบอำเภอ");
			}
			throw new NotFoundException("ไม่พบเขต");
		}
		return subDistrictRepository.findByDistrict_DistrictId(districtId)
				.stream()
				.sorted(Comparator.comparing(SubDistrict::getName))
				.map(this::toSubDistrict)
				.toList();
	}

	public List<GetAddressResponse> getAddressesByUserId(UUID userId) {
		return addressRepository.findByUserIdWithLocation(userId)
				.stream()
				.sorted(Comparator.comparing(Address::getAddressId))
				.map(this::toGetAddressResponse)
				.toList();
	}

	public void createAddress(UUID userId, AddressRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("ไม่พบผู้ใช้"));

		SubDistrict subDistrict = subDistrictRepository.findById(request.getSubDistrictId())
				.orElseThrow(() -> new NotFoundException("ไม่พบตำบล"));

		Address address = Address.builder()
				.user(user)
				.addressName(request.getAddressName())
				.addressDetail(request.getAddressDetail())
				.subDistrict(subDistrict)
				.latitude(request.getLatitude())
				.longitude(request.getLongitude())
				.build();

		addressRepository.save(address);
	}

	public void updateAddress(UUID userId, Integer addressId, AddressRequest request) {
		Address address = addressRepository.findByAddressIdAndUser_UserId(addressId, userId)
				.orElseThrow(() -> new NotFoundException("ไม่พบที่อยู่"));

		SubDistrict subDistrict = subDistrictRepository.findById(request.getSubDistrictId())
				.orElseThrow(() -> new NotFoundException("ไม่พบตำบล"));

		address.setAddressName(request.getAddressName());
		address.setAddressDetail(request.getAddressDetail());
		address.setSubDistrict(subDistrict);
		address.setLatitude(request.getLatitude());
		address.setLongitude(request.getLongitude());
		addressRepository.save(address);
	}

	public void deleteAddress(UUID userId, Integer addressId) {
		Address address = addressRepository.findByAddressIdAndUser_UserId(addressId, userId)
				.orElseThrow(() -> new NotFoundException("ไม่พบที่อยู่"));
		addressRepository.delete(address);
	}
}
