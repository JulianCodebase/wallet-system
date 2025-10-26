package com.wallet.controller;

import com.wallet.dto.response.ApiResponse;
import com.wallet.dto.response.ReconciliationResult;
import com.wallet.mapper.WalletMapper;
import com.wallet.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
@Slf4j
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    private final WalletMapper walletMapper;

    /**
     * 对账查询 - 基于用户ID和币种
     */
    @GetMapping("/reconcile")
    public ApiResponse<ReconciliationResult> reconcile(
            @RequestParam Long userId,
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            ReconciliationResult result = reconciliationService.reconcile(
                    userId, currency, startTime, endTime);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("对账查询异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 多币种对账查询 - 查询用户所有币种的对账情况
     */
    @GetMapping("/reconcile-all")
    public ApiResponse<List<ReconciliationResult>> reconcileAll(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            List<ReconciliationResult> results = reconciliationService.reconcileAll(
                    userId, startTime, endTime);
            return ApiResponse.success(results);
        } catch (Exception e) {
            log.error("多币种对账查询异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
