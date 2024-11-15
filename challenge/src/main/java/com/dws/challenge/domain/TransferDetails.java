package com.dws.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TransferDetails {

	@NotNull(message = "Source account ID is required")
	private String fromAccountId;

	@NotNull(message = "Destination account ID is required")
	private String toAccountId;

	@NotNull(message = "Transfer amount is required")
	@Min(value = 1, message = "Transfer amount must be greater than zero")
	private BigDecimal amount;
}
