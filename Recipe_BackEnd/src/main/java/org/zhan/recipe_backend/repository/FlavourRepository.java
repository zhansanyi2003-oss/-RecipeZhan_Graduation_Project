package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Flavour;
import org.zhan.recipe_backend.entity.Recipe_Flavour;

import java.util.Optional;

@Repository
public interface FlavourRepository extends JpaRepository<Flavour, Long>
{

    Optional<Flavour> findByName(String flavourName);
}
