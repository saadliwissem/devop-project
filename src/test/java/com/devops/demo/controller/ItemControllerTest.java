package com.devops.demo.controller;

import com.devops.demo.entity.Item;
import com.devops.demo.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemController itemController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
    }

    @Test
    void createItem_ShouldReturnItem() throws Exception {
        Item item = new Item("Test Item", "Test Description");
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void getAllItems_ShouldReturnList() throws Exception {
        Item item1 = new Item("Item 1", "Description 1");
        Item item2 = new Item("Item 2", "Description 2");
        
        when(itemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].name").value("Item 2"));
    }

    @Test
    void getItemById_WhenExists_ShouldReturnItem() throws Exception {
        Item item = new Item("Laptop", "Gaming Laptop");
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getItemById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound());
    }
}
