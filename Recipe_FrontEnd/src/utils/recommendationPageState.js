const nonEmptyArray = (value) => Array.isArray(value) && value.length > 0

export const hasTimePreference = (preferences = {}) => {
  const value = preferences?.timeAvailability
  return value !== null && value !== undefined && value !== '' && value !== '999'
}

export const hasTastePreferences = (preferences = {}) => {
  return (
    nonEmptyArray(preferences?.cuisines) ||
    nonEmptyArray(preferences?.flavours) ||
    nonEmptyArray(preferences?.dietary)
  )
}

export const EXPLORE_TAG_CANDIDATES = [
  { key: 'italian', label: 'Italian', type: 'cuisine', value: 'Italian' },
  { key: 'mexican', label: 'Mexican', type: 'cuisine', value: 'Mexican' },
  { key: 'chinese', label: 'Chinese', type: 'cuisine', value: 'Chinese' },
  { key: 'spicy', label: 'Spicy', type: 'flavour', value: 'Spicy' },
  { key: 'savory', label: 'Savory', type: 'flavour', value: 'Savory' },
  { key: 'sweet', label: 'Sweet', type: 'flavour', value: 'Sweet' },
]

export const buildExploreTags = ({
  availableCuisines = [],
  availableFlavours = [],
  limit = 6,
} = {}) => {
  const cuisineSet = new Set(availableCuisines)
  const flavourSet = new Set(availableFlavours)

  return EXPLORE_TAG_CANDIDATES.filter((tag) => {
    if (tag.type === 'cuisine') return cuisineSet.has(tag.value)
    if (tag.type === 'flavour') return flavourSet.has(tag.value)
    return false
  }).slice(0, limit)
}

export const buildRecommendationSections = ({ preferences = {}, savedRecipe = null } = {}) => {
  const timeMode = hasTimePreference(preferences) ? 'personalized' : 'fallback'
  const tasteMode = hasTastePreferences(preferences) ? 'personalized' : 'explore'

  return [
    {
      key: 'right-now',
      mode: timeMode,
      title: timeMode === 'personalized' ? 'Made for right now' : 'Good for this moment',
      subtitle:
        timeMode === 'personalized'
          ? 'Based on your available cooking time and the current time of day'
          : 'Picked for breakfast, lunch, dinner, or late-night cravings',
    },
    savedRecipe?.title
      ? {
          key: 'behavior',
          mode: 'personalized',
          title: `Because you saved "${savedRecipe.title}"`,
          subtitle: 'More recipes with a similar cuisine and vibe',
        }
      : {
          key: 'behavior',
          mode: 'cta',
          title: 'Start personalizing your feed',
          subtitle: 'Set your preferences or save a few recipes to unlock better recommendations.',
        },
    {
      key: 'taste',
      mode: tasteMode,
      title: tasteMode === 'personalized' ? 'Picked for your taste' : 'Explore by taste',
      subtitle:
        tasteMode === 'personalized'
          ? 'Based on the flavors and cuisines you prefer'
          : 'Pick a cuisine or flavor to start shaping your recommendations.',
    },
  ]
}
