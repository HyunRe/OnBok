package com.onbok.book_hub.delivery.presentation.view;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.dto.DeliveryAddressDto;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/view/delivery-addresses")
@RequiredArgsConstructor
public class DeliveryAddressViewController {
    private final DeliveryAddressService deliveryAddressService;

    @GetMapping
    public String list(@CurrentUser User user, Model model) {
        List<DeliveryAddressDto> addresses = deliveryAddressService.getUserDeliveryAddresses(user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("menu", "delivery");
        return "delivery/list";
    }

    @GetMapping("/form")
    public String createForm(Model model) {
        model.addAttribute("menu", "delivery");
        return "delivery/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@CurrentUser User user, @PathVariable Long id, Model model) {
        DeliveryAddressDto address = deliveryAddressService.getDeliveryAddressDto(id);
        model.addAttribute("address", address);
        model.addAttribute("menu", "delivery");
        return "delivery/form";
    }
}
