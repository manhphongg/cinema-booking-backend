package vn.cineshow.repository;

import org.springframework.data.repository.Repository;
import vn.cineshow.model.Customer;

public interface UserRepository extends Repository<Customer,Integer> {
}
