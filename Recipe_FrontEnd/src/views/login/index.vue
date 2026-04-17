<script setup>
import { ref, onMounted } from 'vue'
import { loginApi, registerApi } from '../../api/login.js'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { getTokenExpiryTimestamp } from '../../utils/auth'
import { useSessionStore } from '../../stores/session'

const isSignIn = ref(false)
const router = useRouter()
const route = useRoute()
const sessionStore = useSessionStore()

const loginForm = ref({
  username: '',
  password: '',
})

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const formErrors = ref({})

const rules = {
  register: {
    username: [{ required: true, message: 'Username is required' }],
    email: [
      { required: true, message: 'Email is required' },
      { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Please enter a valid email' },
    ],
    password: [
      { required: true, message: 'Password is required' },
      { minLength: 6, message: 'Password must be at least 6 characters' },
    ],
    confirmPassword: [
      { required: true, message: 'Please confirm your password' },
      { custom: (val) => val === registerForm.value.password, message: 'Passwords do not match' },
    ],
  },
  login: {
    username: [{ required: true, message: 'Please enter your username' }],
    password: [{ required: true, message: 'Please enter your password' }],
  },
}

const validateField = (formType, field) => {
  const currentRules = rules[formType][field]
  const formData = formType === 'register' ? registerForm.value : loginForm.value
  const value = formData[field]

  delete formErrors.value[field]

  for (const rule of currentRules) {
    if (rule.required && !value) {
      formErrors.value[field] = rule.message
      return false
    }
    if (rule.pattern && !rule.pattern.test(value)) {
      formErrors.value[field] = rule.message
      return false
    }
    if (rule.minLength && value.length < rule.minLength) {
      formErrors.value[field] = rule.message
      return false
    }
    if (rule.custom && !rule.custom(value)) {
      formErrors.value[field] = rule.message
      return false
    }
  }

  return true
}

const validateForm = (formType) => {
  let isValid = true
  const currentRules = rules[formType]

  for (const field in currentRules) {
    if (!validateField(formType, field)) {
      isValid = false
    }
  }

  return isValid
}

const clearError = (field) => {
  delete formErrors.value[field]
}

const togglePanel = () => {
  isSignIn.value = !isSignIn.value
}

const handleRegister = async () => {
  if (!validateForm('register')) {
    ElMessage.error('The form can not be Empty')
    return
  }

  const payload = {
    username: registerForm.value.username,
    email: registerForm.value.email,
    password: registerForm.value.password,
  }

  try {
    const result = await registerApi(payload)
    if (result.code) {
      ElMessage.success('You successfully signed up')
      loginForm.value.username = registerForm.value.username
      isSignIn.value = true
      formErrors.value = {}
      registerForm.value.username = ''
      registerForm.value.email = ''
      registerForm.value.password = ''
      registerForm.value.confirmPassword = ''
    } else {
      formErrors.value.username = result.data?.msg || 'Register failed'
    }
  } catch (error) {
    console.error('Register failed:', error)
    ElMessage.error('Register failed. Please check your network or try again.')
  }
}

const handleLogin = async () => {
  if (!validateForm('login')) {
    ElMessage.error('The form can not be Empty or Invalid')
    return
  }

  try {
    const result = await loginApi(loginForm.value)

    if (result.code) {
      ElMessage.success('You successfully logged in')

      const token = result.data
      const loginUserObj = {
        Authorization: token,
        username: loginForm.value.username,
      }
      const tokenExp = getTokenExpiryTimestamp(loginUserObj.Authorization)
      if (!tokenExp) {
        throw new Error('Missing token expiration.')
      }

      localStorage.setItem('loginUser', JSON.stringify(loginUserObj))
      localStorage.setItem('token_exp', String(tokenExp))
      sessionStore.clearProfile()

      const redirectPath = route.query.redirect || '/'
      router.push(redirectPath)
    } else {
      formErrors.value.password = result.msg || 'Wrong username or password'
      ElMessage.error(result.msg || 'Login failed')
    }
  } catch (error) {
    console.error('Login failed:', error)
    formErrors.value.password = 'Wrong username or password. Please check your network or backend.'
  }
}

onMounted(() => {
  setTimeout(() => {
    isSignIn.value = true
  }, 200)
})
</script>

<template>
  <div class="container" :class="{ 'sign-in': isSignIn, 'sign-up': !isSignIn }">
    <div class="row">
      <div class="col align-items-center flex-col sign-up">
        <div class="form-wrapper align-items-center">
          <div class="form sign-up">
            <div class="input-group" :class="{ 'has-error': formErrors.username }">
              <i class="bx bxs-user"></i>
              <input
                type="text"
                v-model="registerForm.username"
                placeholder="Username"
                @blur="validateField('register', 'username')"
                @input="clearError('username')"
              />
            </div>
            <span class="error-text" v-if="formErrors.username">{{ formErrors.username }}</span>

            <div class="input-group" :class="{ 'has-error': formErrors.email }">
              <i class="bx bx-mail-send"></i>
              <input
                type="email"
                v-model="registerForm.email"
                placeholder="Email"
                @blur="validateField('register', 'email')"
                @input="clearError('email')"
              />
            </div>
            <span class="error-text" v-if="formErrors.email">{{ formErrors.email }}</span>

            <div class="input-group" :class="{ 'has-error': formErrors.password }">
              <i class="bx bxs-lock-alt"></i>
              <input
                type="password"
                v-model="registerForm.password"
                placeholder="Password"
                @blur="validateField('register', 'password')"
                @input="clearError('password')"
              />
            </div>
            <span class="error-text" v-if="formErrors.password">{{ formErrors.password }}</span>

            <div class="input-group" :class="{ 'has-error': formErrors.confirmPassword }">
              <i class="bx bxs-lock-alt"></i>
              <input
                type="password"
                v-model="registerForm.confirmPassword"
                placeholder="Confirm password"
                @blur="validateField('register', 'confirmPassword')"
                @input="clearError('confirmPassword')"
              />
            </div>
            <span class="error-text" v-if="formErrors.confirmPassword">{{
              formErrors.confirmPassword
            }}</span>

            <button @click="handleRegister" style="margin-top: 1rem">Sign up</button>
            <p>
              <span> Already have an account? </span>
              <b @click="togglePanel" class="pointer"> Sign in here </b>
            </p>
          </div>
        </div>
      </div>
      <div class="col align-items-center flex-col sign-in">
        <div class="form-wrapper align-items-center">
          <div class="form sign-in">
            <div class="input-group">
              <i class="bx bxs-user"></i>
              <input type="text" v-model="loginForm.username" placeholder="Username" />
            </div>
            <div class="input-group">
              <i class="bx bxs-lock-alt"></i>
              <input type="password" v-model="loginForm.password" placeholder="Password" />
            </div>
            <button @click="handleLogin">Sign in</button>
            <p>
              <b> Forgot password? </b>
            </p>
            <p>
              <span> Don't have an account? </span>
              <b @click="togglePanel" class="pointer"> Sign up here </b>
            </p>
          </div>
        </div>
        <div class="form-wrapper"></div>
      </div>
    </div>
    <div class="row content-row">
      <div class="col align-items-center flex-col">
        <div class="text sign-in">
          <h2>Welcome</h2>
        </div>
        <div class="img sign-in"></div>
      </div>
      <div class="col align-items-center flex-col">
        <div class="img sign-up"></div>
        <div class="text sign-up">
          <h2>Join with us</h2>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@200;300;400;500;600&display=swap');

.container {
  --primary-color: #4ea685;
  --secondary-color: #57b894;
  --black: #000000;
  --white: #ffffff;
  --gray: #efefef;
  --gray-2: #757575;
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
}

* {
  font-family: 'Poppins', sans-serif;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.row {
  display: flex;
  flex-wrap: wrap;
  height: 100dvh;
}

.col {
  width: 50%;
}

.align-items-center {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.form-wrapper {
  width: 100%;
  max-width: 28rem;
}

.form {
  padding: 1rem;
  background-color: var(--white);
  border-radius: 1.5rem;
  width: 100%;
  box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px;
  transform: scale(0);
  transition: 0.5s ease-in-out;
  transition-delay: 1s;
}

.input-group {
  position: relative;
  width: 100%;
  margin: 1rem 0;
}

/* 注意：这个模板用了 Boxicons 的图标类名，
   如果你的项目没引入 Boxicons，图标会不显示。
   可以在 index.html 引入：
   <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
*/
.input-group i {
  position: absolute;
  top: 50%;
  left: 1rem;
  transform: translateY(-50%);
  font-size: 1.4rem;
  color: var(--gray-2);
}

.input-group input {
  width: 100%;
  padding: 1rem 3rem;
  font-size: 1rem;
  background-color: var(--gray);
  border-radius: 0.5rem;
  border: 0.125rem solid var(--white);
  outline: none;
}

.input-group input:focus {
  border: 0.125rem solid var(--primary-color);
}

.form button {
  cursor: pointer;
  width: 100%;
  padding: 0.6rem 0;
  border-radius: 0.5rem;
  border: none;
  background-color: var(--primary-color);
  color: var(--white);
  font-size: 1.2rem;
  outline: none;
}

.form p {
  margin: 1rem 0;
  font-size: 0.7rem;
}

.flex-col {
  flex-direction: column;
}

.pointer {
  cursor: pointer;
}

.container.sign-in .form.sign-in,
.container.sign-up .form.sign-up {
  transform: scale(1);
}

.content-row {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 6;
  width: 100%;
}

.text {
  margin: 4rem;
  color: var(--white);
}

.text h2 {
  font-size: 3.5rem;
  font-weight: 800;
  margin: 2rem 0;
  transition: 1s ease-in-out;
}

.text p {
  font-weight: 600;
  transition: 1s ease-in-out;
  transition-delay: 0.2s;
}

.img img {
  width: 30vw;
  transition: 1s ease-in-out;
  transition-delay: 0.4s;
}

.text.sign-in h2,
.text.sign-in p,
.img.sign-in img {
  transform: translateX(-50vw);
}

.text.sign-up h2,
.text.sign-up p,
.img.sign-up img {
  transform: translateX(50vw);
}

.container.sign-in .text.sign-in h2,
.container.sign-in .text.sign-in p,
.container.sign-in .img.sign-in img,
.container.sign-up .text.sign-up h2,
.container.sign-up .text.sign-up p,
.container.sign-up .img.sign-up img {
  transform: translateX(0);
}

/* BACKGROUND */
.container::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  height: 100dvh;
  width: 300vw;
  transform: translate(35%, 0);
  background-image: linear-gradient(-45deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  transition: 1s ease-in-out;
  z-index: 6;
  box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px;
  border-bottom-right-radius: max(50vw, 50vh);
  border-top-left-radius: max(50vw, 50vh);
}

.container.sign-in::before {
  transform: translate(0, 0);
  right: 50%;
}

.container.sign-up::before {
  transform: translate(100%, 0);
  right: 50%;
}

.input-group.has-error input {
  border: 0.125rem solid #ff4d4f !important;
  background-color: #fff1f0 !important; /* 背景微微变红 */
}

/* 2. 错误时的图标也变红 */
.input-group.has-error i {
  color: #ff4d4f !important;
}

/* 3. 输入框下方的红字提示 */
.error-text {
  display: block;
  width: 100%;
  text-align: left;
  color: #ff4d4f;
  font-size: 0.75rem;
  margin-top: -0.8rem;
  margin-bottom: 0.5rem;
  padding-left: 1rem;
  animation: shake 0.3s ease-in-out;
}

/* 4. 消除原 input-group 过大的下边距，给错误文字留位置 */
.input-group {
  margin: 1rem 0 1.2rem 0;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  50% {
    transform: translateX(4px);
  }
  75% {
    transform: translateX(-4px);
  }
}

/* RESPONSIVE */
@media only screen and (max-width: 768px) {
  .container::before,
  .container.sign-in::before,
  .container.sign-up::before {
    width: 100vw;
    height: 100dvh;
    border-radius: 0;
    transform: none !important;
    right: 0 !important;
  }
  .img {
    display: none !important;
  }
  .col {
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    height: 100dvh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 1.5rem;

    /* 未激活的面板：往下掉一点点，变透明，并且鼠标穿透（不可点击） */
    opacity: 0;
    pointer-events: none;
    transform: translateY(30px);
    transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .container.sign-in .col.sign-in,
  .container.sign-up .col.sign-up {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
    z-index: 10;
  }

  .content-row {
    position: absolute;
    top: 12%;
    height: auto;
    z-index: 11;
  }
  .form {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    padding: 2.5rem 1.5rem;
    border-radius: 1.2rem;
    margin: 0;
  }

  .content-row .col {
    position: relative;
    height: auto;
    padding: 0;
    transform: none !important;
  }

  .row {
    align-items: flex-end;
    justify-content: flex-end;
  }

  .text {
    margin: 0;
    text-align: center;
  }

  .text p {
    font-size: 0.95rem;
    margin-top: 0.5rem;
    display: block;
  }

  .text h2 {
    font-size: 2.4rem;
    margin: 0;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}
</style>

