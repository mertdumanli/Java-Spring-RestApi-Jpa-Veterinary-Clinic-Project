package com.works.repositories._jpa;

import com.works.entities.security.ForgotPasswordUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordUserRepository extends JpaRepository<ForgotPasswordUser, String> {
}
