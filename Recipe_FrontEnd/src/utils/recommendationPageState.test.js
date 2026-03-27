import assert from 'node:assert/strict'
import {
  buildExploreTags,
  buildRecommendationSections,
  hasTastePreferences,
  hasTimePreference,
} from './recommendationPageState.js'

const runTimePreferenceCheck = () => {
  assert.equal(hasTimePreference({ timeAvailability: '30' }), true)
  assert.equal(hasTimePreference({ timeAvailability: 20 }), true)
  assert.equal(hasTimePreference({ timeAvailability: '' }), false)
  assert.equal(hasTimePreference({ timeAvailability: null }), false)
  assert.equal(hasTimePreference({ timeAvailability: '999' }), false)
}

const runTastePreferenceCheck = () => {
  assert.equal(hasTastePreferences({ cuisines: ['Italian'] }), true)
  assert.equal(hasTastePreferences({ flavours: ['Spicy'] }), true)
  assert.equal(hasTastePreferences({ dietary: ['Vegetarian'] }), true)
  assert.equal(hasTastePreferences({ cuisines: [], flavours: [], dietary: [] }), false)
}

const runColdStartSections = () => {
  const sections = buildRecommendationSections({
    preferences: { cuisines: [], flavours: [], dietary: [], timeAvailability: '' },
    savedRecipe: null,
  })

  assert.deepEqual(
    sections.map((section) => ({ key: section.key, mode: section.mode, title: section.title })),
    [
      { key: 'right-now', mode: 'fallback', title: 'Good for this moment' },
      { key: 'behavior', mode: 'cta', title: 'Start personalizing your feed' },
      { key: 'taste', mode: 'explore', title: 'Explore by taste' },
    ],
  )
}

const runPersonalizedSections = () => {
  const sections = buildRecommendationSections({
    preferences: {
      cuisines: ['Italian'],
      flavours: ['Savory'],
      dietary: [],
      timeAvailability: '25',
    },
    savedRecipe: { title: 'Spaghetti Carbonara' },
  })

  assert.deepEqual(
    sections.map((section) => ({ key: section.key, mode: section.mode, title: section.title })),
    [
      { key: 'right-now', mode: 'personalized', title: 'Made for right now' },
      {
        key: 'behavior',
        mode: 'personalized',
        title: 'Because you saved "Spaghetti Carbonara"',
      },
      { key: 'taste', mode: 'personalized', title: 'Picked for your taste' },
    ],
  )
}

const runPartialSections = () => {
  const sections = buildRecommendationSections({
    preferences: {
      cuisines: ['Mexican'],
      flavours: [],
      dietary: [],
      timeAvailability: '',
    },
    savedRecipe: null,
  })

  assert.equal(sections[0].mode, 'fallback')
  assert.equal(sections[1].mode, 'cta')
  assert.equal(sections[2].mode, 'personalized')
}

const runExploreTags = () => {
  const tags = buildExploreTags({
    availableCuisines: ['Italian', 'Chinese'],
    availableFlavours: ['Spicy', 'Sweet'],
    limit: 4,
  })

  assert.deepEqual(tags, [
    { key: 'italian', label: 'Italian', type: 'cuisine', value: 'Italian' },
    { key: 'chinese', label: 'Chinese', type: 'cuisine', value: 'Chinese' },
    { key: 'spicy', label: 'Spicy', type: 'flavour', value: 'Spicy' },
    { key: 'sweet', label: 'Sweet', type: 'flavour', value: 'Sweet' },
  ])
}

runTimePreferenceCheck()
runTastePreferenceCheck()
runColdStartSections()
runPersonalizedSections()
runPartialSections()
runExploreTags()

console.log('recommendationPageState tests passed')
