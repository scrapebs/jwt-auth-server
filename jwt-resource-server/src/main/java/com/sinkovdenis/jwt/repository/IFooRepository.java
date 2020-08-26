package com.sinkovdenis.jwt.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.sinkovdenis.jwt.model.Foo;

public interface IFooRepository extends PagingAndSortingRepository<Foo, Long> {
}
