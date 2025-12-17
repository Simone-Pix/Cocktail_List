package com.cocktail.cocktaillist.repository;

import com.cocktail.cocktaillist.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    
    Optional<Color> findByName(String name);
    
    Optional<Color> findByHexCode(String hexCode);
    
    boolean existsByName(String name);
    
    boolean existsByHexCode(String hexCode);

    Color save(Color color);

    void deleteById(Long id);
}
