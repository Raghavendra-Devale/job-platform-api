package com.jobrecommendation.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.repository.RecentViewRepository;
import com.jobrecommendation.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecentViewRepository recentViewRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenEmailExists() {

        UserEntity user = new UserEntity();
        user.setEmail("raghu@test.com");

        when(userRepository.findByEmail("raghu@test.com"))
                .thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findUserByEmail("raghu@test.com");

        assertTrue(result.isPresent());

        assertEquals(
                "raghu@test.com",
                result.get().getEmail());

        verify(userRepository)
                .findByEmail("raghu@test.com");
    }

}
