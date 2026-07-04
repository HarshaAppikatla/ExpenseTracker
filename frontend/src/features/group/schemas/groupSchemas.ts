import { z } from 'zod';
import { GROUP_NAME_MAX_LENGTH, GROUP_DESC_MAX_LENGTH, ROOM_CODE_LENGTH } from '../constants/groupConstants';

export const createGroupSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Group name is required')
    .max(GROUP_NAME_MAX_LENGTH, `Name cannot exceed ${GROUP_NAME_MAX_LENGTH} characters`),
  description: z
    .string()
    .trim()
    .max(GROUP_DESC_MAX_LENGTH, `Description cannot exceed ${GROUP_DESC_MAX_LENGTH} characters`)
    .default(''),
  currency: z
    .string()
    .trim()
    .min(1, 'Currency is required')
    .length(3, 'Currency must be a 3-letter currency code (e.g. USD)'),
});

export const joinGroupSchema = z.object({
  roomCode: z
    .string()
    .trim()
    .min(1, 'Room code is required')
    .length(ROOM_CODE_LENGTH, `Room code must be exactly ${ROOM_CODE_LENGTH} characters`)
    .regex(/^[A-Z2-9]+$/, 'Room code contains invalid characters'),
});

export type CreateGroupInput = z.infer<typeof createGroupSchema>;
export type JoinGroupInput = z.infer<typeof joinGroupSchema>;
