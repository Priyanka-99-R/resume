# Coding Problems — Solutions (JavaScript & Java)

Common coding-round problems for a Wipro lateral interview, easy → medium, with approach, complexity, and clean code.

## Tips for the coding round
- **Clarify first:** ask about input type, size, edge cases (empty, null, negatives, duplicates).
- **Think aloud:** explain your approach before coding — interviewers grade reasoning.
- **Start simple, then optimize:** a brute-force that works beats an elegant solution that doesn't.
- **State complexity:** mention time & space (Big-O) for your solution.
- **Dry-run:** walk through one example to prove correctness.
- **Handle edge cases:** empty array, single element, all duplicates, etc.

---

# Strings

### Problem: Reverse a string
**Approach:** Two pointers, or use built-ins. **Complexity:** O(n) time, O(n) space.
```js
function reverseString(str) {
  return str.split("").reverse().join("");
}
// Without built-ins:
function reverse(str) {
  let res = "";
  for (let i = str.length - 1; i >= 0; i--) res += str[i];
  return res;
}
```
```java
public static String reverse(String str) {
    return new StringBuilder(str).reverse().toString();
}
```

### Problem: Check if a string is a palindrome
**Approach:** Compare characters from both ends. **Complexity:** O(n) time, O(1) space.
```js
function isPalindrome(str) {
  let i = 0, j = str.length - 1;
  while (i < j) {
    if (str[i] !== str[j]) return false;
    i++; j--;
  }
  return true;
}
```
```java
public static boolean isPalindrome(String s) {
    int i = 0, j = s.length() - 1;
    while (i < j) {
        if (s.charAt(i) != s.charAt(j)) return false;
        i++; j--;
    }
    return true;
}
```

### Problem: Count vowels and consonants
**Complexity:** O(n).
```js
function countVowelsConsonants(str) {
  let vowels = 0, consonants = 0;
  for (const ch of str.toLowerCase()) {
    if ("aeiou".includes(ch)) vowels++;
    else if (ch >= "a" && ch <= "z") consonants++;
  }
  return { vowels, consonants };
}
```

### Problem: Check if two strings are anagrams
**Approach:** Sort both, or compare character frequency. **Complexity:** O(n) with frequency map.
```js
function isAnagram(a, b) {
  if (a.length !== b.length) return false;
  const count = {};
  for (const ch of a) count[ch] = (count[ch] || 0) + 1;
  for (const ch of b) {
    if (!count[ch]) return false;
    count[ch]--;
  }
  return true;
}
```
```java
public static boolean isAnagram(String a, String b) {
    if (a.length() != b.length()) return false;
    char[] x = a.toCharArray(), y = b.toCharArray();
    Arrays.sort(x); Arrays.sort(y);
    return Arrays.equals(x, y);
}
```

### Problem: First non-repeating character
**Complexity:** O(n).
```js
function firstNonRepeating(str) {
  const count = {};
  for (const ch of str) count[ch] = (count[ch] || 0) + 1;
  for (const ch of str) if (count[ch] === 1) return ch;
  return null;
}
```

### Problem: Remove duplicate characters from a string
```js
function removeDuplicates(str) {
  return [...new Set(str)].join("");
}
```

### Problem: Reverse words in a sentence
```js
function reverseWords(sentence) {
  return sentence.trim().split(/\s+/).reverse().join(" ");
}
```

### Problem: Check if two strings are rotations of each other
**Approach:** `b` is a rotation of `a` if it's a substring of `a + a`. **Complexity:** O(n).
```js
function areRotations(a, b) {
  return a.length === b.length && (a + a).includes(b);
}
```

### Problem: Count occurrences of each character (frequency map)
```js
function charFrequency(str) {
  const map = {};
  for (const ch of str) map[ch] = (map[ch] || 0) + 1;
  return map;
}
```

---

# Arrays

### Problem: Find the maximum and minimum in an array
**Complexity:** O(n).
```js
function maxMin(arr) {
  return { max: Math.max(...arr), min: Math.min(...arr) };
}
```

### Problem: Find the second largest element
**Approach:** Single pass, track largest & second largest. **Complexity:** O(n).
```js
function secondLargest(arr) {
  let first = -Infinity, second = -Infinity;
  for (const n of arr) {
    if (n > first) { second = first; first = n; }
    else if (n > second && n !== first) second = n;
  }
  return second;
}
```

### Problem: Reverse an array in place
```js
function reverseArray(arr) {
  let i = 0, j = arr.length - 1;
  while (i < j) { [arr[i], arr[j]] = [arr[j], arr[i]]; i++; j--; }
  return arr;
}
```

### Problem: Find the missing number (1..n)
**Approach:** Sum of 1..n minus actual sum. **Complexity:** O(n) time, O(1) space.
```js
function missingNumber(arr, n) {
  const expected = (n * (n + 1)) / 2;
  const actual = arr.reduce((a, b) => a + b, 0);
  return expected - actual;
}
```

### Problem: Two Sum (return indices of two numbers adding to target)
**Approach:** HashMap of value → index. **Complexity:** O(n).
```js
function twoSum(nums, target) {
  const map = new Map();
  for (let i = 0; i < nums.length; i++) {
    const need = target - nums[i];
    if (map.has(need)) return [map.get(need), i];
    map.set(nums[i], i);
  }
  return [];
}
```
```java
public static int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int need = target - nums[i];
        if (map.containsKey(need)) return new int[]{map.get(need), i};
        map.put(nums[i], i);
    }
    return new int[]{};
}
```

### Problem: Move all zeros to the end
**Complexity:** O(n).
```js
function moveZeros(arr) {
  let pos = 0;
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] !== 0) { [arr[pos], arr[i]] = [arr[i], arr[pos]]; pos++; }
  }
  return arr;
}
```

### Problem: Find duplicates in an array
```js
function findDuplicates(arr) {
  const seen = new Set(), dups = new Set();
  for (const n of arr) seen.has(n) ? dups.add(n) : seen.add(n);
  return [...dups];
}
```

### Problem: Maximum subarray sum (Kadane's algorithm)
**Approach:** Track current sum; reset if it drops below 0. **Complexity:** O(n).
```js
function maxSubArray(nums) {
  let maxSoFar = nums[0], current = nums[0];
  for (let i = 1; i < nums.length; i++) {
    current = Math.max(nums[i], current + nums[i]);
    maxSoFar = Math.max(maxSoFar, current);
  }
  return maxSoFar;
}
```

### Problem: Rotate an array by k positions
**Complexity:** O(n).
```js
function rotate(arr, k) {
  k = k % arr.length;
  return [...arr.slice(-k), ...arr.slice(0, -k)];
}
```

### Problem: Merge two sorted arrays
**Complexity:** O(n + m).
```js
function mergeSorted(a, b) {
  const res = []; let i = 0, j = 0;
  while (i < a.length && j < b.length) {
    res.push(a[i] <= b[j] ? a[i++] : b[j++]);
  }
  return [...res, ...a.slice(i), ...b.slice(j)];
}
```

### Problem: Find a pair with a given sum
```js
function hasPairWithSum(arr, sum) {
  const set = new Set();
  for (const n of arr) {
    if (set.has(sum - n)) return true;
    set.add(n);
  }
  return false;
}
```

### Problem: Majority element (appears > n/2 times) — Boyer-Moore
**Complexity:** O(n) time, O(1) space.
```js
function majorityElement(nums) {
  let count = 0, candidate = null;
  for (const n of nums) {
    if (count === 0) candidate = n;
    count += (n === candidate) ? 1 : -1;
  }
  return candidate;
}
```

---

# Numbers

### Problem: Factorial (iterative + recursive)
```js
function factorialIter(n) { let r = 1; for (let i = 2; i <= n; i++) r *= i; return r; }
function factorialRec(n) { return n <= 1 ? 1 : n * factorialRec(n - 1); }
```
```java
public static long factorial(int n) {
    long r = 1;
    for (int i = 2; i <= n; i++) r *= i;
    return r;
}
```

### Problem: Fibonacci (iterative + recursive + memoized)
```js
function fibIter(n) {
  let a = 0, b = 1;
  for (let i = 0; i < n; i++) [a, b] = [b, a + b];
  return a;
}
function fibMemo(n, memo = {}) {
  if (n <= 1) return n;
  if (memo[n]) return memo[n];
  return memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
}
```

### Problem: Check if a number is prime
**Complexity:** O(√n).
```js
function isPrime(n) {
  if (n < 2) return false;
  for (let i = 2; i * i <= n; i++) if (n % i === 0) return false;
  return true;
}
```

### Problem: Print primes in a range
```js
function primesInRange(start, end) {
  const res = [];
  for (let n = start; n <= end; n++) if (isPrime(n)) res.push(n);
  return res;
}
```

### Problem: Armstrong number (e.g., 153 = 1³+5³+3³)
```js
function isArmstrong(n) {
  const digits = String(n).split("");
  const p = digits.length;
  const sum = digits.reduce((a, d) => a + Math.pow(+d, p), 0);
  return sum === n;
}
```

### Problem: Check if a number is a palindrome
```js
function isNumberPalindrome(n) {
  const s = String(n);
  return s === s.split("").reverse().join("");
}
```

### Problem: Swap two numbers without a temp variable
```js
let a = 5, b = 10;
a = a + b; b = a - b; a = a - b; // a=10, b=5
// or with XOR: a ^= b; b ^= a; a ^= b;
```

### Problem: GCD and LCM
```js
function gcd(a, b) { return b === 0 ? a : gcd(b, a % b); }
function lcm(a, b) { return (a * b) / gcd(a, b); }
```

### Problem: Count digits & sum of digits
```js
function sumOfDigits(n) {
  let sum = 0;
  n = Math.abs(n);
  while (n > 0) { sum += n % 10; n = Math.floor(n / 10); }
  return sum;
}
```

### Problem: FizzBuzz
```js
function fizzBuzz(n) {
  for (let i = 1; i <= n; i++) {
    if (i % 15 === 0) console.log("FizzBuzz");
    else if (i % 3 === 0) console.log("Fizz");
    else if (i % 5 === 0) console.log("Buzz");
    else console.log(i);
  }
}
```

---

# Sorting & Searching

### Problem: Bubble sort
**Complexity:** O(n²).
```js
function bubbleSort(arr) {
  for (let i = 0; i < arr.length - 1; i++)
    for (let j = 0; j < arr.length - 1 - i; j++)
      if (arr[j] > arr[j + 1]) [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]];
  return arr;
}
```

### Problem: Binary search (sorted array)
**Complexity:** O(log n).
```js
function binarySearch(arr, target) {
  let lo = 0, hi = arr.length - 1;
  while (lo <= hi) {
    const mid = Math.floor((lo + hi) / 2);
    if (arr[mid] === target) return mid;
    if (arr[mid] < target) lo = mid + 1;
    else hi = mid - 1;
  }
  return -1;
}
```
```java
public static int binarySearch(int[] arr, int target) {
    int lo = 0, hi = arr.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) lo = mid + 1;
        else hi = mid - 1;
    }
    return -1;
}
```

### Problem: Quicksort (idea)
Pick a **pivot**, partition the array so smaller elements go left and larger go right, then recursively sort each partition. Average **O(n log n)**, worst case O(n²).

---

# HashMap-based

### Problem: Group anagrams
**Approach:** Use sorted word as the map key. **Complexity:** O(n·k log k).
```js
function groupAnagrams(words) {
  const map = {};
  for (const w of words) {
    const key = w.split("").sort().join("");
    (map[key] ||= []).push(w);
  }
  return Object.values(map);
}
```

### Problem: First repeating element
```js
function firstRepeating(arr) {
  const seen = new Set();
  for (const n of arr) {
    if (seen.has(n)) return n;
    seen.add(n);
  }
  return null;
}
```

---

# Recursion

### Problem: Sum of array elements (recursive)
```js
function sumArray(arr, i = 0) {
  return i === arr.length ? 0 : arr[i] + sumArray(arr, i + 1);
}
```

### Problem: Power(x, n)
**Approach:** Fast exponentiation. **Complexity:** O(log n).
```js
function power(x, n) {
  if (n === 0) return 1;
  const half = power(x, Math.floor(n / 2));
  return n % 2 === 0 ? half * half : x * half * half;
}
```

---

# Stack / Linked List

### Problem: Check balanced parentheses
**Approach:** Push opening brackets, pop & match on closing. **Complexity:** O(n).
```js
function isBalanced(s) {
  const stack = [];
  const pairs = { ")": "(", "]": "[", "}": "{" };
  for (const ch of s) {
    if ("([{".includes(ch)) stack.push(ch);
    else if (pairs[ch]) {
      if (stack.pop() !== pairs[ch]) return false;
    }
  }
  return stack.length === 0;
}
```

### Problem: Reverse a linked list (Java)
**Complexity:** O(n) time, O(1) space.
```java
class Node { int val; Node next; Node(int v){ val = v; } }

public static Node reverse(Node head) {
    Node prev = null, curr = head;
    while (curr != null) {
        Node next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev; // new head
}
```

### Problem: Find the duplicate in an array (Floyd's cycle / set)
```js
function findDuplicate(nums) {
  const seen = new Set();
  for (const n of nums) {
    if (seen.has(n)) return n;
    seen.add(n);
  }
  return -1;
}
```
