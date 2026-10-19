package com.seyahood.exception;

/**
 * Giriş denemesi doğru şifreyle yapıldı ama hesap henüz e-posta ile
 * doğrulanmamış. Frontend'in kullanıcıyı doğrulama ekranına
 * yönlendirebilmesi için e-postayı da taşır.
 */
public class AccountNotVerifiedException extends RuntimeException {
    private final String email;

    public AccountNotVerifiedException(String email) {
        super("ACCOUNT_NOT_VERIFIED");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
