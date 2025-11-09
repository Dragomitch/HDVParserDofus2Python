/**
 * Price entry model representing a single price point for an item
 */
export interface PriceEntryDTO {
  id: number;
  price: number;
  quantity: number;
  createdAt: string;
}
