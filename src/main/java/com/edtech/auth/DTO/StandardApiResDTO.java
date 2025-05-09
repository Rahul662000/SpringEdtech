package com.edtech.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardApiResDTO<T> {
        private boolean success;
        private String message;
        private T data;

        public StandardApiResDTO(boolean success, String message) {
                this.success = success;
                this.message = message;
        }
}
