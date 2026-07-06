import { z } from 'zod';

export const createTripSchema = z.object({
  title: z.string().min(1, 'Title is required').max(100, 'Title cannot exceed 100 characters'),
  description: z.string().max(255, 'Description cannot exceed 255 characters').optional(),
  city: z.string().min(1, 'City is required').max(100, 'City cannot exceed 100 characters'),
  country: z.string().min(1, 'Country is required').max(100, 'Country cannot exceed 100 characters'),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().min(1, 'End date is required'),
  coverType: z.enum(['PRESET', 'CUSTOM']).default('PRESET'),
  coverImage: z.string().max(255).optional(),
  allowInvites: z.boolean().default(true),
  visibility: z.enum(['GROUP', 'PRIVATE']).default('GROUP'),
}).refine((data) => {
  const start = new Date(data.startDate);
  const end = new Date(data.endDate);
  return start <= end;
}, {
  message: "End date must be after or equal to start date",
  path: ["endDate"],
});

export type CreateTripInput = z.infer<typeof createTripSchema>;
export default createTripSchema;
