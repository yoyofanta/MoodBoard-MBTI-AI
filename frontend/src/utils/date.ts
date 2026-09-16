export function localDateString(value = new Date()) {
  const date = new Date(value)
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function compareCalendarDate(left: string, right = localDateString()) {
  return left.localeCompare(right)
}

export function isFutureDate(value: string, today = localDateString()) {
  return compareCalendarDate(value, today) > 0
}
