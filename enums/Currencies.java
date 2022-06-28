package com.arsinex.com.enums;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public enum Currencies {
    TRY {
        @NonNull
        @NotNull
        @Override
        public String toString() {
            return "TRY";
        }
    },
    USD {
        @NonNull
        @NotNull
        @Override
        public String toString() {
            return "USD";
        }
    },
    EUR {
        @NonNull
        @NotNull
        @Override
        public String toString() {
            return "EUR";
        }
    },
    GBP {
        @NonNull
        @NotNull
        @Override
        public String toString() {
            return "GBP";
        }
    }

}
