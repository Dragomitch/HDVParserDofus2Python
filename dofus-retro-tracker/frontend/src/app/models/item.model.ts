import { CategoryDTO } from './category.model';

/**
 * Item model representing a Dofus item with its details
 */
export interface ItemDTO {
  id: number;
  itemGid: number;
  itemName: string;
  category: CategoryDTO | null;
  createdAt: string;
  updatedAt: string | null;
}
