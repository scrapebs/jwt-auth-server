package com.sinkovdenis.jwt.service;

import java.util.Optional;

import com.sinkovdenis.jwt.model.Foo;


public interface IFooService {
    Optional<Foo> findById(Long id);

    Foo save(Foo foo);
    
    Iterable<Foo> findAll();

}
