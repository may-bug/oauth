export interface OAuth2Client {
  id: number
  clientId: string
  clientSecret?: string
  clientName: string
  clientType: 1 | 2 | 3
  redirectUris: string
  allowedScopes: string
  ownerId?: number
  accessTokenTtl: number
  refreshTokenTtl: number
  status: 0 | 1
  createdAt?: string
  updatedAt?: string
}
