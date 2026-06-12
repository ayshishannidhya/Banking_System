package com.asp.userservice.service;

import com.asp.userservice.DTO.UsersDTO.UsersRequestDTO;
import com.asp.userservice.DTO.UsersDTO.UsersResponseDTO;
import com.asp.userservice.enumeration.MaritalStatus;
import com.asp.userservice.mappers.UsersMapper;
import com.asp.userservice.models.Users;
import com.asp.userservice.repositories.KycRepository;
import com.asp.userservice.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private UserRepository userRepository;
    private KycRepository kycRepository;
    private UsersMapper usersMapper;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setKycRepository(KycRepository kycRepository) {
        this.kycRepository = kycRepository;
    }

    @Autowired
    public void setUsersMapper(UsersMapper usersMapper) {
        this.usersMapper = usersMapper;
    }

    @Transactional
    public void registerUser(
            UsersRequestDTO request,
            MultipartFile aadhaar,
            MultipartFile pan,
            MultipartFile photo,
            MultipartFile signature,
            MultipartFile voterId,
            MultipartFile passportId,
            MultipartFile drivingLicenseId
    ) throws Exception {
        if (request == null || request.getContactDetails() == null || request.getNominee() == null) {
            throw new IllegalArgumentException("User, contact details and nominee cannot be null.");
        }

        if (request.getMaritalStatus() == MaritalStatus.MARRIED && request.getSpouseName() == null) {
            throw new IllegalArgumentException("Spouse name is required for married users.");
        }

        Users users = UsersMapper.toEntity(request);

        if (users.getContactDetails() == null) {
            throw new IllegalStateException("Mapped contact details are null.");
        }
        if (users.getNominee() == null) {
            throw new IllegalStateException("Mapped nominee details are null.");
        }
        if (users.getKycId() == null) {
            throw new IllegalStateException("Mapped KYC details are null.");
        }

        users.getContactDetails().setUser(users);
        users.getNominee().setUser(users);
        users.getKycId().setUser(users);

        users.getKycId().setAadharImage(aadhaar.getBytes());
        users.getKycId().setPanImage(pan.getBytes());
        users.getKycId().setUserPhoto(photo.getBytes());
        users.getKycId().setUserSignature(signature.getBytes());

        if (voterId != null && !voterId.isEmpty()) {
            users.getKycId().setVoterIdImage(voterId.getBytes());
        }
        if (passportId != null && !passportId.isEmpty()) {
            users.getKycId().setPassportImage(passportId.getBytes());
        }
        if (drivingLicenseId != null && !drivingLicenseId.isEmpty()) {
            users.getKycId().setDrivingLicenseImage(drivingLicenseId.getBytes());
        }

        userRepository.save(users);
    }

    public UsersResponseDTO getUserById(Long userId) {
        Users user = userRepository.findByUserid(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        return UsersMapper.toResponseDTO(user);
    }

    public List<UsersResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(UsersMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsersResponseDTO updateUser(Long userId, UsersRequestDTO dto) {
        Users user = userRepository.findByUserid(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) user.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getDateOfBirth() != null) user.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getFatherName() != null) user.setFatherName(dto.getFatherName());
        if (dto.getMotherName() != null) user.setMotherName(dto.getMotherName());
        if (dto.getMaritalStatus() != null) user.setMaritalStatus(dto.getMaritalStatus());
        if (dto.getSpouseName() != null) user.setSpouseName(dto.getSpouseName());
        if (dto.getOccupation() != null) user.setOccupation(dto.getOccupation());
        if (dto.getSalary() != null) user.setSalary(dto.getSalary());
        if (dto.getCitizen() != null) user.setCitizen(dto.getCitizen());
        if (dto.getCategory() != null) user.setCategory(dto.getCategory());
        if (dto.getReligion() != null) user.setReligion(dto.getReligion());

        if (dto.getContactDetails() != null) {
            var cd = user.getContactDetails();
            var cdDto = dto.getContactDetails();
            if (cdDto.getMobileNumber() != null) cd.setMobileNumber(cdDto.getMobileNumber());
            if (cdDto.getEmail() != null) cd.setEmail(cdDto.getEmail());
            if (cdDto.getCommunicationAddress() != null) cd.setCommunicationAddress(cdDto.getCommunicationAddress());
            if (cdDto.getPermanentAddress() != null) cd.setPermanentAddress(cdDto.getPermanentAddress());
            if (cdDto.getCity() != null) cd.setCity(cdDto.getCity());
            if (cdDto.getState() != null) cd.setState(cdDto.getState());
            if (cdDto.getZip() != null) cd.setZip(cdDto.getZip());
            if (cdDto.getLandmark() != null) cd.setLandmark(cdDto.getLandmark());
            if (cdDto.getCountry() != null) cd.setCountry(cdDto.getCountry());
        }

        Users saved = userRepository.save(user);
        return UsersMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        Users user = userRepository.findByUserid(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        user.setIsDeleted(true);
        user.setIsActive(false);
        userRepository.save(user);
    }

    public boolean userExists(Long userId) {
        return userRepository.existsByUserid(userId);
    }
}
