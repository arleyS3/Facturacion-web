"use client";

import * as React from "react";
import { Check, ChevronsUpDown } from "lucide-react";

import { cn } from "@/components/ui/utils";
import { Button } from "@/components/ui/button";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

export interface CatalogComboboxOption {
  code: string;
  label: string;
  extra?: string;
}

export interface CatalogComboboxProps {
  id?: string;
  value: string;
  options: CatalogComboboxOption[];
  onValueChange: (code: string) => void;
  placeholder?: string;
  searchPlaceholder?: string;
  emptyMessage?: string;
  loading?: boolean;
  disabled?: boolean;
  className?: string;
  triggerClassName?: string;
  contentClassName?: string;
  ariaLabel?: string;
}

/**
 * Combobox reutilizable para selección de catálogos usando shadcn Command + Popover.
 *
 * - Muestra las opciones como `CODE - Label`
 * - Búsqueda cmdk nativa por código y label
 * - Ancho del popover igual al trigger (via CSS var)
 * - Si `value` no coincide con ninguna opción, muestra el raw value
 */
export function CatalogCombobox({
  id,
  value,
  options,
  onValueChange,
  placeholder = "Seleccione...",
  searchPlaceholder = "Buscar...",
  emptyMessage = "Sin resultados",
  loading = false,
  disabled = false,
  className,
  triggerClassName,
  contentClassName,
  ariaLabel,
}: CatalogComboboxProps) {
  const [open, setOpen] = React.useState(false);

  const selected = options.find((opt) => opt.code === value);
  const displayValue = selected
    ? `${selected.code} - ${selected.label}`
    : value || placeholder;

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          id={id}
          variant="outline"
          role="combobox"
          aria-expanded={open}
          aria-label={ariaLabel ?? placeholder}
          disabled={disabled}
          className={cn(
            "w-full justify-between font-normal",
            !value && "text-muted-foreground",
            triggerClassName,
          )}
        >
          {loading
            ? "Cargando..."
            : options.length === 0 && !value
              ? placeholder
              : displayValue}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className={cn(
          "w-[var(--radix-popover-trigger-width)] p-0",
          contentClassName,
        )}
        align="start"
      >
        <Command className={className}>
          <CommandInput placeholder={searchPlaceholder} />
          <CommandList>
            <CommandEmpty>{emptyMessage}</CommandEmpty>
            <CommandGroup>
              {options.map((option) => (
                <CommandItem
                  key={option.code}
                  value={`${option.code} ${option.label}`}
                  onSelect={() => {
                    onValueChange(option.code);
                    setOpen(false);
                  }}
                >
                  <Check
                    className={cn(
                      "mr-2 h-4 w-4",
                      value === option.code ? "opacity-100" : "opacity-0",
                    )}
                  />
                  {option.code} - {option.label}
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
