package vn.cineshow.service;


import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.model.Account;

public interface AccountService {
    public long createCustomerAccount(EmailRegisterRequest req);

}
