export type PracticeStatus = 'not-started' | 'skeleton' | 'planned' | 'ready'

export interface PracticeChapter {
  number: string
  title: string
  status: PracticeStatus
  note?: string
  to?: string
}

export interface PracticeGroup {
  id: string
  label: string
  range: string
  chapters: PracticeChapter[]
}
