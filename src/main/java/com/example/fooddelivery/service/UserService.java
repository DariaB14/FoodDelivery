package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.UserMapper;
import com.example.fooddelivery.dto.request.UserRequest;
import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.entity.Review;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.UserRole;
import com.example.fooddelivery.exception.exceptions.EmailException;
import com.example.fooddelivery.exception.exceptions.EntityNotFoundException;
import com.example.fooddelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse register(UserRequest userRequest){
        if(userRepository.existsByEmail(userRequest.getEmail())){
            throw new EmailException("Email is already in use");
        }

        User user = userMapper.toEntity(userRequest);
        User registeredUser = userRepository.save(user);
        return userMapper.toDto(registeredUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", id)));
        if (!user.isActive()){
            throw new EntityNotFoundException(String.format("User with if %d is deactivated", id));
        }
        return userMapper.toDto(user);
    }

    public UserResponse update(Long id, UserRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", id)));

        if(request.getEmail()!=null && !request.getEmail().equals(user.getEmail())){
            if(userRepository.existsByEmail(request.getEmail())){
                throw new EmailException("Email is already in use");
            }
        }

        userMapper.update(request, user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findByRole(UserRole role){
        List<User> users = userRepository.findByRole(role);

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deactivate(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", id)));

        user.setActive(false);
        userRepository.save(user);
    }
}
